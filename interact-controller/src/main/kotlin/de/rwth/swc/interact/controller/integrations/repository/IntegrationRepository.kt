package de.rwth.swc.interact.controller.integrations.repository

import com.fasterxml.jackson.databind.AbstractTypeResolver
import com.fasterxml.jackson.databind.ObjectMapper
import de.rwth.swc.interact.controller.integrations.dto.*
import de.rwth.swc.interact.controller.persistence.domain.ABSTRACT_TEST_CASE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.INTERACTION_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.service.ConcreteTestCaseDao
import de.rwth.swc.interact.domain.*
import org.neo4j.driver.Value
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.util.*

@Component
class IntegrationRepository(
    private val neo4jClient: Neo4jClient,
    private val concreteTestCaseDao: ConcreteTestCaseDao,
    private val objectMapper: ObjectMapper
) {

    fun findByMissingValidationCandidate() : Collection<InteractionExpectationInfo> {
        return neo4jClient.query(
            "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL{validated:false}) " +
                    "WHERE NOT (ie)-[:POTENTIALLY_VALIDATED_BY]->(:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{nextTest:NOT NULL}) " +
                    "MATCH (m_from)<-[:EXPECT_FROM]-(ie)-[:EXPECT_TO]->(m_to) " +
                    "WITH ie, m_from.id as from_id, collect(m_to.id) as to_ids " +
                    "RETURN ie, from_id, to_ids"
        ).fetchAs(InteractionExpectationInfo::class.java).mappedBy { _, record ->
            InteractionExpectationInfo(
                InteractionExpectationId(UUID.fromString(record.get("ie").asNode().get("id").asString())),
                MessageId(UUID.fromString(record.get("from_id").asString())),
                record.get("to_ids").asList {
                    MessageId(UUID.fromString(it.asString()))
                }
            )
        }.all()
    }

    fun determineTestInvocationDescriptor(interactionTestInfo: InteractionTestInfo, executedTests: List<ConcreteTestCaseId>): TestInvocationDescriptor {
        val replacements = interactionTestInfo.replacements.map { replacement ->
            @Suppress("UNCHECKED_CAST")
            neo4jClient.query(
                "MATCH (ctc)-[:TRIGGERED]->(m)-[:SENT_BY]->(oi{id:\"${replacement.value}\"}) WHERE ctc.id IN \$testCases " +
                        "MATCH (om{id:\"${replacement.key}\"}) " +
                        "WITH om,m,oi,labels(om) as omlabels, labels(m) as mlabels " +
                        "RETURN om{.*, labels:omlabels},m{.*,labels:mlabels},oi"
            ).bind(executedTests.map { it.toString() }).to("testCases")
                .fetchAs(Pair::class.java)
                .mappedBy { _, record ->
                    Pair(
                        mapToReceivedMessage(record.get("om"), record.get("oi")),
                        mapToSentMessage(record.get("m"), record.get("oi"))
                    )
                }.first().orElseThrow() as Pair<ReceivedMessage, SentMessage>
        }.toMap()
        val atc = findAbstractTestCaseByConcreteTestCaseId(interactionTestInfo.testCaseId)
        val messages = concreteTestCaseDao.findById(interactionTestInfo.testCaseId)!!.observedMessages
        return TestInvocationDescriptor(
            atc,
            messages.filter { it.messageType != MessageType.Sent.COMPONENT_RESPONSE }.map {
                val key = replacements.keys.firstOrNull { k -> k.value == it.value }
                if (key == null) {
                    it.value
                } else {
                    replacements[key]!!.value
                }
            }.toList()
        )
    }

    fun findAbstractTestCaseByConcreteTestCaseId(testCaseId: ConcreteTestCaseId): AbstractTestCase {
       return  neo4jClient.query(
            "MATCH (atc)-[:USED_TO_DERIVE]->(ctc) WHERE ctc.id = \$ctc_id " +
                    "RETURN atc"
        ).bind(testCaseId.toString()).to("ctc_id")
           .fetchAs(AbstractTestCase::class.java).mappedBy{_,record ->
            AbstractTestCase(
                AbstractTestCaseSource(record.get("atc").asNode().get("source").asString()),
                AbstractTestCaseName(record.get("atc").asNode().get("name").asString())
            ).also {
                it.id = AbstractTestCaseId(UUID.fromString(record.get("atc").asNode().get("id").asString()))
            }
        }.first().orElseGet{ throw RuntimeException() }
    }

    fun findInteractionTestsToExecuteForComponent(componentId: ComponentId): List<TestInvocationDescriptor> {
        return neo4jClient.query(
            "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{nextComponent:\$componentId}) " +
                    "RETURN vp.nextTest as nextTest")
            .bind(componentId.toString()).to("componentId")
            .fetchAs(TestInvocationDescriptor::class.java).mappedBy{_,record ->
                objectMapper.readValue(record.get("nextTest").asString(), TestInvocationDescriptor::class.java)
            }.all().toList()
    }

    private fun mapToReceivedMessage(value: Value, _interface: Value): ReceivedMessage {
        return ReceivedMessage(
            if (value.get("labels").asList()
                    .contains(MessageType.Received.STIMULUS.name)
            ) MessageType.Received.STIMULUS else MessageType.Received.ENVIRONMENT_RESPONSE,
            MessageValue(value.get("payload").asString()),
            IncomingInterface(
                Protocol(_interface.get("protocol").asString()),
                ProtocolData(_interface.keys().filter { it.startsWith("protocolData") }
                    .associate { Pair(it.replaceFirst("protocolData.", ""), _interface.get(it).asString()) })
            )
        ).also {
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
        ).also {
            it.id = MessageId(UUID.fromString(value.get("id").asString()))
        }
    }

    fun deriveInteractionExpectations() {
        val getComponentResponsesWithoutExistingInteractonExpectations =
            "MATCH (cmp_res:${MessageType.Sent.COMPONENT_RESPONSE})<-[:TRIGGERED]-(:UNITTest) " +
            "WHERE NOT (cmp_res)<-[:EXPECT_FROM]-() "
        val getThePathFromEachComponentResponseOverAllEnvironmentResponsesThatAreAResponseToThatComponentResponse =
            "CALL apoc.path.expand(cmp_res,\"NEXT>\",\"${MessageType.Received.ENVIRONMENT_RESPONSE}\",1,-1) " +
            "YIELD path " +
            "WITH cmp_res, length(path) as len, path " +
            "WITH cmp_res, apoc.agg.maxItems(path,len) as longestPath "
        neo4jClient.query(getComponentResponsesWithoutExistingInteractonExpectations +
                getThePathFromEachComponentResponseOverAllEnvironmentResponsesThatAreAResponseToThatComponentResponse +
                    "WITH nodes(longestPath.items[0]) as elems " +
                    "WITH head(elems) as h, randomUUID() as ieid, tail(elems) as t, range(0, size(elems)-2) as idxs " +
                    "UNWIND idxs as idx " +
                    "WITH h, ieid, idx, t[idx] as elem " +
                    "MERGE (h)<-[:EXPECT_FROM]-(ie:$INTERACTION_EXPECTATION_NODE_LABEL{id:ieid,validated:false}) " +
                    "MERGE (ie)-[:EXPECT_TO{order:idx}]->(elem)"
        ).run()
    }

    fun expandPathFrom(currentPathEnd: MessageId): List<PathStepInfo> {
        return neo4jClient.query(
            "MATCH (:${MessageType.Sent.COMPONENT_RESPONSE} {id:\"$currentPathEnd\"})-[:SENT_BY]->(oi)-[:BOUND_TO]->(ii)<-[:RECEIVED_BY]-(m)<-[:TRIGGERED]-(ctc:UNITTest) " +
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
    }


    fun findTerminalNodesForTargetMessage(messageId: MessageId): List<MessageId> {
        return neo4jClient.query(
            "MATCH (:UNITTest)-[:TRIGGERED]->(tn:${MessageType.Sent.COMPONENT_RESPONSE})-[:SENT_BY]->()-[:BOUND_TO]->()<-[:RECEIVED_BY]-(:${MessageType.Received.ENVIRONMENT_RESPONSE}{id:\"" + messageId + "\"}) RETURN tn.id as id"
        ).fetchAs(MessageId::class.java).mappedBy{_, record -> MessageId(UUID.fromString(record.get("id").asString()))}
            .all().toList().distinct()
    }

    fun updateInterfaceExpectationValidationPlanWithNewExecution(vp: InteractionExpectationValidationPlan, concreteTestCase: ConcreteTestCase) {
        if(concreteTestCase.result != TestResult.SUCCESS) {
            setValidationStatusOfValidationPlan(vp.id!!, false)
            return
        }

        val nextTestManipulation = objectMapper.readValue(vp.interactionPathInfo, InteractionPathInfo::class.java).interactionTests.getOrNull(vp.testedPath.size+1)
        val nextTestCaseId = nextTestManipulation?.testCaseId
        val testedPath = vp.testedPath + concreteTestCase.id!!

        neo4jClient.query(
            "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{id:\"${vp.id}\"}) SET vp.testedPath=\$testedPath"
        ).bind(testedPath.map { it.toString() }).to("testedPath").run()

        if(nextTestCaseId == null) {
            setValidationStatusOfValidationPlan(vp.id!!, true)
            return
        }

        val nextTest = objectMapper.writeValueAsString(determineTestInvocationDescriptor(nextTestManipulation, testedPath))
        neo4jClient.query(
            "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{id:\"${vp.id}\"}) SET " +
                    "vp.nextTest=\$nextTest REMOVE vp.nextComponent"
        ).bind(nextTest).to("nextTest").run()
    }

    private fun setValidationStatusOfValidationPlan(vpId: InteractionExpectationValidationPlanId, valid: Boolean) {
        if(valid) {
            neo4jClient.query(
                "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{id:\"$vpId\"}) " +
                        "MATCH (ie:$INTERACTION_EXPECTATION_NODE_LABEL)-[:POTENTIALLY_VALIDATED_BY]->(vp) " +
                        "SET vp.validated = true, ie.validated = true " +
                        "REMOVE vp.nextComponent, vp.nextTest"
            ).run()
        } else {
            neo4jClient.query(
                "MATCH (vp:$INTERACTION_EXPECTATION_VALIDATION_PLAN_NODE_LABEL{id:\"$vpId\"}) " +
                        "REMOVE vp.nextComponent, vp.nextTest"
            ).run()
        }
    }

    fun findComponentForAbstractTestCaseId(testCaseId: AbstractTestCaseId): ComponentId {
        return neo4jClient.query(
            "MATCH (c)-[:TESTED_BY]->(:$ABSTRACT_TEST_CASE_NODE_LABEL{id:\"$testCaseId\"}) RETURN c.id as id"
        ).fetchAs(ComponentId::class.java)
            .mappedBy { _, record -> ComponentId(UUID.fromString(record.get("id").asString())) }
            .first().orElseThrow()
    }

}