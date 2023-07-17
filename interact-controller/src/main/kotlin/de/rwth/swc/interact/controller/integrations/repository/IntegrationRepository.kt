package de.rwth.swc.interact.controller.integrations.repository

import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.controller.integrations.dto.*
import de.rwth.swc.interact.controller.persistence.domain.COMPONENT_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.INTERACTION_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.neo4j.driver.Value
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.util.*

@Component
class IntegrationRepository(private val neo4jClient: Neo4jClient, private val objectMapper: ObjectMapper) {

    fun findReplacementsForComponent(name: ComponentName, version: ComponentVersion): List<TestInvocationsDescriptor> {

        val ies = neo4jClient.query(
            "MATCH (c:$COMPONENT_NODE_LABEL{name:\"$name\",version:\"$version\"})-[:TESTED_BY]->(atc)-[:USED_TO_DERIVE]->(ctc) " +
                    "WITH atc,collect(ctc.id) as testCases " +
                    "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL) WHERE ie.nextTest IN testCases " +
                    "RETURN ie.interactionPathInfo as pathInfo, ie.id as id, ie.testedPath as testedPath"
        ).fetchAs(Triple::class.java).mappedBy { _, record ->
            Triple(
                InteractionExpectationId(UUID.fromString(record.get("id").asString())),
                objectMapper.readValue(record.get("pathInfo").asString(), InteractionPathInfo::class.java),
                if (record.get("testedPath").isNull) listOf() else record.get("testedPath").asList()
                    .map { UUID.fromString(it.toString()) }
            )
        }.all()

        return ies.map { ie ->
            val replacements =
                (ie.second as InteractionPathInfo).interactionTests[(ie.third as List<*>).size].replacements.map { replacement ->
                    @Suppress("UNCHECKED_CAST")
                    neo4jClient.query(
                        "MATCH (ctc)-[:TRIGGERED]->(m)-[:SENT_BY]->(oi{id:\"${replacement.value}\"}) WHERE ctc.id IN \$testCases " +
                                "MATCH (om{id:\"${replacement.key}\"}) " +
                                "WITH om,m,oi,labels(om) as omlabels, labels(m) as mlabels " +
                                "RETURN om{.*, labels:omlabels},m{.*,labels:mlabels},oi"
                    ).bind((ie.third as List<*>).map { it.toString() }).to("testCases")
                        .fetchAs(Pair::class.java)
                        .mappedBy { _, record ->
                            Pair(
                                mapToReceivedMessage(record.get("om"), record.get("oi")),
                                mapToSentMessage(record.get("m"), record.get("oi"))
                            )
                        }.first().orElseGet { throw RuntimeException() } as Pair<ReceivedMessage, SentMessage>
                }.toMap()
            val ieId = ie.first as InteractionExpectationId
            val tr = neo4jClient.query(
                "MATCH (ie{id:\"$ieId\"}) " +
                        "MATCH (atc)-[:USED_TO_DERIVE]->(ctc) WHERE ctc.id = ie.nextTest " +
                        "RETURN atc,ctc"
            ).fetchAs(AbstractTestCase::class.java).mappedBy { _, record ->
                AbstractTestCase(
                    source = AbstractTestCaseSource(record.get("atc").get("source").asString()),
                    name = AbstractTestCaseName(record.get("atc").get("name").asString())
                )
            }.first().orElseGet { throw RuntimeException() }
            TestInvocationsDescriptor(
                tr,
                listOf(
                    TestInvocationDescriptor(
                        ie.first as InteractionExpectationId,
                        replacements
                    )
                )
            )
        }
    }

    private fun mapToReceivedMessage(value: Value, _interface: Value): ReceivedMessage {
        return ReceivedMessage(
            if(value.get("labels").asList().contains(MessageType.Received.STIMULUS.name)) MessageType.Received.STIMULUS else MessageType.Received.ENVIRONMENT_RESPONSE,
            MessageValue(value.get("payload").asString()),
            IncomingInterface(
                Protocol(_interface.get("protocol").asString()),
                ProtocolData(_interface.keys().filter { it.startsWith("protocolData") }
                    .associate { Pair(it.replaceFirst("protocolData.", ""), _interface.get(it).asString()) })
            )
        ).also { it ->
            it.id = MessageId(UUID.fromString(value.get("id").asString()))
        }
    }

    private fun mapToSentMessage(value: Value, _interface: Value): SentMessage {
        return SentMessage(
            MessageType.Sent.COMPONENT_RESPONSE,
            MessageValue(value.get("payload").asString()),
            OutgoingInterface(
                Protocol(_interface.get("protocol").asString()),
                ProtocolData(_interface.keys().filter { it.startsWith("protocolData") }
                    .associate { Pair(it.replaceFirst("protocolData.", ""), _interface.get(it).asString()) })
            )
        ).also { m ->
            m.id = MessageId(UUID.fromString(value.get("id").asString()))
        }
    }

    fun deriveInteractionExpectations() {
        neo4jClient.query(
            "MATCH (cmp_res:${MessageType.Sent.COMPONENT_RESPONSE})<-[:TRIGGERED]-(:UNITTest) " +
                    "WHERE NOT (cmp_res)<-[:EXPECT_FROM]-() " +
                    "CALL apoc.path.expand(cmp_res,\"NEXT>\",\"${MessageType.Received.ENVIRONMENT_RESPONSE}\",1,-1) " +
                    "YIELD path as paths " +
                    "UNWIND paths as path " +
                    "WITH apoc.path.elements(path) as elems " +
                    "WITH head(elems) as h, last(elems) as l " +
                    "MERGE (h)<-[:EXPECT_FROM]-(:$INTERACTION_EXPECTATION_NODE_LABEL{id:randomUUID(),validated:false})-[:EXPECT_TO]->(l)"
        ).run()
    }

    fun findUnvalidatedInteractionExpectationsWithoutPathCandidate(): Collection<InteractionExpectationInfo> {
        return neo4jClient.query(
            "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{validated:false}) " +
                    "WHERE ie.interactionPathInfo IS NULL " +
                    "Match (ie)-[:EXPECT_FROM]->(from)<-[:TRIGGERED]-(ctc)" +
                    "MATCH (ie)-[:EXPECT_TO]->(to) " +
                    "RETURN ie.id as id ,from.id as fromId,to.id as toId,ctc.id as baseTest, ie.interactionPathQueue as queue"
        ).fetchAs(InteractionExpectationInfo::class.java)
            .mappedBy { _, record ->
                InteractionExpectationInfo(
                    InteractionExpectationId(UUID.fromString(record.get("id").asString())),
                    MessageId(UUID.fromString(record.get("fromId").asString())),
                    MessageId(UUID.fromString(record.get("toId").asString())),
                    ConcreteTestCaseId(UUID.fromString(record.get("baseTest").asString())),
                    if(!record.get("queue").isNull)
                        objectMapper.readerForListOf(InteractionPathInfo::class.java).readValue(record.get("queue").asString())
                    else
                        listOf()
                )
            }.all()
    }

    fun findNewInteractionExpectationPathCandidate(ie: InteractionExpectationInfo): InteractionExpectationValidationStatus? {

        val terminalNodes = findTerminalNodes(ie)
        val queue = LinkedList<InteractionPathInfo>()

        if(ie.queue.isNotEmpty()) {
            ie.queue.forEach { queue.offer(it) }
        } else {
            val currentInteractionPath = mutableListOf<InteractionTestInfo>()
            currentInteractionPath.add(InteractionTestInfo(
                ie.baseTest,
                ie.fromId,
                mapOf()))
            queue.offer(InteractionPathInfo(currentInteractionPath))
        }
        while (!queue.isEmpty()) {
            val searchPath = queue.poll()
            val pathEnd = searchPath.interactionTests[searchPath.interactionTests.size - 1].nextStart
            if (terminalNodes.contains(pathEnd)) {
                //füge den basis case mit der finalen ersetzung zu der liste der integrationstests hinzu
                var replacementMessageInterfaceId = neo4jClient.query(
                    "MATCH (m:${MessageType.Sent.COMPONENT_RESPONSE} {id:\"$pathEnd\"}) " +
                            "WITH m " +
                            "MATCH (m)-[:SENT_BY]->(oi) " +
                            "RETURN oi.id as id"
                ).fetchAs(InterfaceId::class.java)
                    .mappedBy{
                        _, record ->
                        InterfaceId(UUID.fromString(record.get("id").asString()))
                    }.first().get()

                val newTestCaseReplacements = searchPath.testCaseReplacements.getOrElse(ie.baseTest) {
                    mapOf()
                }.plus(
                    Pair(
                        ie.toId,
                        replacementMessageInterfaceId
                    )
                )

                val newReplacements = searchPath.testCaseReplacements.toMutableMap()
                newReplacements[ie.baseTest] = newTestCaseReplacements

                return InteractionExpectationValidationStatus(
                    InteractionPathInfo(
                        searchPath.interactionTests.plus(
                            InteractionTestInfo(
                                ie.baseTest,
                                null,
                                newReplacements[ie.baseTest]!!
                            )
                        ),
                        searchPath.visitedInterfaces.plus(replacementMessageInterfaceId),
                        searchPath.visitedComponentTestCase.toMap(),
                        newReplacements.toMap()
                    ),
                    queue.toList()
                )
            }
            val next = neo4jClient.query(
                "MATCH (:${MessageType.Sent.COMPONENT_RESPONSE} {id:\"$pathEnd\"})-[:SENT_BY]->(oi)-[:BOUND_TO]->(ii)<-[:RECEIVED_BY]-(m)<-[:TRIGGERED]-(ctc:UNITTest) " +
                        "MATCH (ii)<-[:PROVIDES]-(c) " +
                        "CALL apoc.path.expand(m,\"NEXT>\",\"${MessageType.Sent.COMPONENT_RESPONSE}\",1,-1) " +
                        "YIELD path as paths " +
                        "UNWIND paths as path " +
                        "WITH apoc.path.elements(path) as elems, m, oi, ii, c, ctc " +
                        "RETURN last(elems).id as next, m.id as original, oi.id as replacement, c.id as component, ctc.id as testcase, ii.id as interface"
            ).fetchAs(PathStepInfo::class.java)
                .mappedBy { _, record ->
                    PathStepInfo(
                        MessageId(UUID.fromString(record.get("next").asString())),
                        MessageId(UUID.fromString(record.get("original").asString())),
                        InterfaceId(UUID.fromString(record.get("replacement").asString())),
                        ComponentId(UUID.fromString(record.get("component").asString())),
                        ConcreteTestCaseId(UUID.fromString(record.get("testcase").asString())),
                        InterfaceId(UUID.fromString(record.get("interface").asString())),
                    )
                }.all().toList()
            for (n in next) {
                if (isNotVisited(n, searchPath)) {
                    var newReplacements: Map<ConcreteTestCaseId, Map<MessageId, InterfaceId>>
                    if (searchPath.testCaseReplacements[n.testCase] == null) {
                        newReplacements = searchPath.testCaseReplacements.plus(
                            Pair(
                                n.testCase,
                                mapOf(Pair(n.originalMessage, n.replacementMessageInterface))
                            )
                        )
                    } else {
                        val newTestCaseReplacements = searchPath.testCaseReplacements[n.testCase]!!.plus(
                            Pair(
                                n.originalMessage,
                                n.replacementMessageInterface
                            )
                        )
                        newReplacements = searchPath.testCaseReplacements.toMutableMap()
                        newReplacements.replace(n.testCase, newTestCaseReplacements)
                    }
                    val ip = InteractionPathInfo(
                        searchPath.interactionTests.plus(
                            InteractionTestInfo(
                                n.testCase,
                                n.next,
                                newReplacements[n.testCase]!!
                            )
                        ),
                        searchPath.visitedInterfaces.plus(n.originalMessageInterface),
                        if (searchPath.visitedComponentTestCase[n.component] == null) {
                            searchPath.visitedComponentTestCase.plus(Pair(n.component, n.testCase))
                        } else {
                            searchPath.visitedComponentTestCase
                        },
                        newReplacements
                    )
                    queue.offer(ip)
                }
            }
        }
        return null
    }

    private fun findTerminalNodes(ie: InteractionExpectationInfo): List<MessageId> {
        return neo4jClient.query(
            "MATCH (:UNITTest)-[:TRIGGERED]->(tn:${MessageType.Sent.COMPONENT_RESPONSE})-[:SENT_BY]->()-[:BOUND_TO]->()<-[:RECEIVED_BY]-(:${MessageType.Received.ENVIRONMENT_RESPONSE}{id:\"" + ie.toId + "\"}) RETURN tn.id as id"
        ).fetchAs(MessageId::class.java).mappedBy{_, record -> MessageId(UUID.fromString(record.get("id").asString()))}
            .all().toList()
    }

    private fun isNotVisited(stepInfo: PathStepInfo, searchPath: InteractionPathInfo): Boolean {
        return !searchPath.visitedInterfaces.contains(stepInfo.originalMessageInterface) &&
                (
                        searchPath.visitedComponentTestCase[stepInfo.component] == null
                                || searchPath.visitedComponentTestCase[stepInfo.component] == stepInfo.testCase
                        )
    }

    fun setPathInfoForExpectation(it: InteractionExpectationInfo, pathInfo: InteractionExpectationValidationStatus?) {
        if (pathInfo != null) {
            neo4jClient.query(
                "MATCH (n {id:\"${it.id}\"}) SET n.interactionPathInfo=\$pathinfo, n.interactionPathQueue=\$queue, n.nextTest=\$nextTest, n.testedPath=\$testedPath"
            ).bind(pathInfo.interactionPathInfo.let { objectMapper.writeValueAsString(it) }).to("pathinfo")
                .bind(pathInfo.interactionPathQueue.let { objectMapper.writeValueAsString(it) }).to("queue")
                .bind(pathInfo.interactionPathInfo.interactionTests[1].testCaseId.toString()).to("nextTest")
                .bind(listOf(pathInfo.interactionPathInfo.interactionTests[0].testCaseId.toString())).to("testedPath")
                .run()
        } else {
            neo4jClient.query(
                "MATCH (n {id:\"${it.id}\"}) REMOVE n.interactionPathInfo, n.interactionPathQueue, n.nextTest, n.testedPath, n.testedPath"
            ).run()
        }
    }

    fun findInteractionExpectationPathInfoAndTestedPath(interactionExpectationId: InteractionExpectationId): Pair<InteractionPathInfo, List<ConcreteTestCaseId>> {
        @Suppress("UNCHECKED_CAST")
        return neo4jClient.query(
            "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{id:\"$interactionExpectationId\"}) RETURN ie.interactionPathInfo as path, ie.testedPath as testedPath"
        ).fetchAs(Pair::class.java)
            .mappedBy { _, record ->
                Pair(
                    objectMapper.readValue(record.get("path").asString(), InteractionPathInfo::class.java),
                    record.get("testedPath").asList().map { ConcreteTestCaseId(UUID.fromString(it.toString())) }
                )
            }.first().orElseThrow() as Pair<InteractionPathInfo, List<ConcreteTestCaseId>>
    }

    fun updateInterfaceExpectationInfo(interactionExpectationId: InteractionExpectationId, testedPath: List<ConcreteTestCaseId>, nextTest: ConcreteTestCaseId?) {
        if(nextTest == null) {
            tryValidateInteractionExpectation(interactionExpectationId)
        }
        neo4jClient.query(
            "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{id:\"$interactionExpectationId\"}) SET ie.testedPath=\$testedPath" +
                    if (nextTest != null) ", ie.nextTest=\"$nextTest\"" else " REMOVE ie.nextTest"
        )
            .bind(testedPath.map { it.toString() }).to("testedPath")
            .run()
    }

    private fun tryValidateInteractionExpectation(interactionExpectationId: InteractionExpectationId) {
        val valid = neo4jClient.query(
            "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{id:\"$interactionExpectationId\"}) " +
                    "MATCH (ctc:ConcreteTestCase) WHERE ctc.id IN ie.testedPath " +
                    "RETURN ctc.result AS result"
        ).fetchAs(Boolean::class.java)
            .mappedBy { _, record ->  record.get("result").asString() == TestResult.SUCCESS.name }
            .all().reduce { acc, b -> acc && b }
        if(valid) {
            neo4jClient.query(
                "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{id:\"$interactionExpectationId\"}) SET ie.validated = \"true\""
            ).run()
        }
    }

}