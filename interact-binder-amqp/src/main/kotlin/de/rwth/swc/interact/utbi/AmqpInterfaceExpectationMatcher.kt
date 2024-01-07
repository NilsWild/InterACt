package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class AmqpInterfaceExpectationMatcher(
    private val neo4jClient: Neo4jClient,
    private val amqpInterfaceMatcher: AmqpInterfaceMatcher,
    private val amqpInterfaceRepository: AmqpInterfaceRepository,
    private val amqpInterfaceExpectationRepository: AmqpInterfaceExpectationRepository
): InterfaceExpectationMatcher {
    override val name: InterfaceExpectationMatcherName
        get() = InterfaceExpectationMatcherName("AmqpMatcher")
    override val version: InterfaceExpectationMatcherVersion
        get() = InterfaceExpectationMatcherVersion("1.0.0")

    override fun match(expectation: InterfaceExpectation) {
        when (expectation) {
            is IncomingInterfaceExpectation -> matchOutgoingInterfaces(
                listOf(expectation),
                amqpInterfaceRepository.findAllOutgoingInterfaces()
            )

            is OutgoingInterfaceExpectation -> matchIncomingInterfaces(
                listOf(expectation),
                amqpInterfaceRepository.findAllIncomingInterfaces()
            )
        }
    }

    override fun match(componentInterface: ComponentInterface) {
        when(componentInterface) {
            is IncomingInterface -> matchIncomingInterfaces(
                amqpInterfaceExpectationRepository.findAllOutgoingInterfaceExpectations(),
                listOf(componentInterface)
            )

            is OutgoingInterface -> matchOutgoingInterfaces(
                amqpInterfaceExpectationRepository.findAllIncomingInterfaceExpectations(),
                listOf(componentInterface)
            )
        }
    }

    private fun matchIncomingInterfaces(
        expectations: Collection<OutgoingInterfaceExpectation>,
        incomingInterfaces: Collection<IncomingInterface>
    ) {
        for (expectation in expectations) {
            for (incomingInterface in incomingInterfaces) {
                if (canHandle(expectation) && canHandle(incomingInterface)) {
                    if (amqpInterfaceMatcher.matchAMQPInterfaces(
                            expectation.protocolData,
                            incomingInterface.protocolData
                        )
                    ) {
                        neo4jClient.query(
                            "MATCH (ie:$OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL {id: \$ieId}) " +
                                    "OPTIONAL MATCH (i:$INCOMING_INTERFACE_NODE_LABEL {id: \$inId}) " +
                                    "MERGE (ie)-[:MATCHED_BY{createdBy:'$name:$version'}]->(i)"
                        ).bind(expectation.id!!.id.toString()).to("ieId")
                            .bind(incomingInterface.id!!.id.toString()).to("inId")
                            .run()
                    }
                }
            }
        }
    }

    private fun matchOutgoingInterfaces(
        expectations: Collection<IncomingInterfaceExpectation>,
        outgoingInterfaces: Collection<OutgoingInterface>
    ) {
        for(expectation in expectations) {
            for (outgoingInterface in outgoingInterfaces) {
                if (canHandle(expectation) && canHandle(outgoingInterface)) {
                    if (amqpInterfaceMatcher.matchAMQPInterfaces(
                            outgoingInterface.protocolData,
                            expectation.protocolData
                        )
                    ) {
                        neo4jClient.query(
                            "MATCH (ie:$INCOMING_INTERFACE_EXPECTATION_NODE_LABEL {id: \$ieId}) " +
                                    "OPTIONAL MATCH (oi:$OUTGOING_INTERFACE_NODE_LABEL {id: \$outId}) " +
                                    "MERGE (ie)-[:MATCHED_BY{createdBy:'$name:$version'}]->(oi)"
                        ).bind(expectation.id!!.id.toString()).to("ieId")
                            .bind(outgoingInterface.id!!.id.toString()).to("outId")
                            .run()
                    }
                }
            }
        }
    }

    override fun canHandle(expectation: InterfaceExpectation): Boolean {
        return expectation.protocol.protocol == "AMQP"
    }

    override fun canHandle(componentInterface: ComponentInterface): Boolean {
        return componentInterface.protocol.protocol == "AMQP"
    }
}