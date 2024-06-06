package de.interact.utbi

import de.interact.controller.persistence.domain.*
import de.interact.domain.amqp.IncomingAmqpInterface
import de.interact.domain.amqp.IncomingAmqpInterfaceExpectation
import de.interact.domain.amqp.OutgoingAmqpInterface
import de.interact.domain.amqp.OutgoingAmqpInterfaceExpectation
import de.interact.domain.expectations.specification.events.InterfaceExpectationAddedEvent
import de.interact.domain.shared.*
import de.interact.domain.testtwin.api.event.IncomingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.OutgoingInterfaceAddedToVersionEvent
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class AmqpInterfaceExpectationMatcher(
    private val neo4jClient: Neo4jClient,
    private val amqpInterfaceMatcher: AmqpInterfaceMatcher,
    private val amqpInterfaceRepository: AmqpInterfaceRepository,
    private val amqpInterfaceExpectationRepository: AmqpInterfaceExpectationRepository
) : InterfaceExpectationMatcher {

    override fun match(event: InterfaceExpectationAddedEvent): List<Pair<InterfaceExpectationId, InterfaceId>> {
        return when (event) {
            is InterfaceExpectationAddedEvent.IncomingInterfaceExpectationAddedEvent -> matchIncomingInterfaces(
                listOf(
                    amqpInterfaceExpectationRepository.findIncomingAmqpInterfaceExpectation(event.id.id)!!
                ),
                amqpInterfaceRepository.findAllIncomingAmqpInterfaces()
            )

            is InterfaceExpectationAddedEvent.OutgoingInterfaceExpectationAddedEvent -> matchOutgoingInterfaces(
                listOf(
                    amqpInterfaceExpectationRepository.findOutgoingAmqpInterfaceExpectation(event.id.id)!!
                ),
                amqpInterfaceRepository.findAllOutgoingAmqpInterfaces()
            )
        }
    }

    override fun match(event: InterfaceAddedToVersionEvent): List<Pair<InterfaceExpectationId, InterfaceId>> {
        return when (event) {
            is IncomingInterfaceAddedToVersionEvent -> matchIncomingInterfaces(
                amqpInterfaceExpectationRepository.findAllIncomingAmqpInterfaceExpectations(),
                listOf(
                    amqpInterfaceRepository.findIncomingInterface(event.interfaceId)!!
                )
            )

            is OutgoingInterfaceAddedToVersionEvent -> matchOutgoingInterfaces(
                amqpInterfaceExpectationRepository.findAllOutgoingAmqpInterfaceExpectations(),
                listOf(
                    amqpInterfaceRepository.findOutgoingInterface(event.interfaceId)!!
                )
            )
        }
    }

    private fun matchIncomingInterfaces(
        expectations: Collection<IncomingAmqpInterfaceExpectation>,
        incomingInterfaces: Collection<IncomingAmqpInterface>
    ): List<Pair<InterfaceExpectationId, InterfaceId>> {
        val result = mutableListOf<Pair<InterfaceExpectationId, InterfaceId>>()
        for (expectation in expectations) {
            for (incomingInterface in incomingInterfaces) {

                if (amqpInterfaceMatcher.matchAMQPInterfaces(
                        expectation.protocolData,
                        incomingInterface.protocolData
                    )
                ) {
                    neo4jClient.query(
                        "MATCH (ie:$INCOMING_INTERFACE_EXPECTATION_NODE_LABEL {id: \$ieId}) " +
                                "OPTIONAL MATCH (i:$INCOMING_INTERFACE_NODE_LABEL {id: \$inId}) " +
                                "MERGE (ie)-[:$MATCHED_BY_RELATIONSHIP_LABEL]->(i)"
                    ).bind(expectation.id.toString()).to("ieId")
                        .bind(incomingInterface.id.toString()).to("inId")
                        .run()
                    result.add(IndirectIncomingInterfaceExpectationId(expectation.id) to IncomingInterfaceId(incomingInterface.id))
                }
            }
        }
        return result
    }

    private fun matchOutgoingInterfaces(
        expectations: Collection<OutgoingAmqpInterfaceExpectation>,
        outgoingInterfaces: Collection<OutgoingAmqpInterface>
    ): List<Pair<InterfaceExpectationId, InterfaceId>> {
        val result = mutableListOf<Pair<InterfaceExpectationId, InterfaceId>>()
        for (expectation in expectations) {
            for (outgoingInterface in outgoingInterfaces) {

                if (amqpInterfaceMatcher.matchAMQPInterfaces(
                        outgoingInterface.protocolData,
                        expectation.protocolData
                    )
                ) {
                    neo4jClient.query(
                        "MATCH (ie:$OUTGOING_INTERFACE_EXPECTATION_NODE_LABEL {id: \$ieId}) " +
                                "OPTIONAL MATCH (oi:$OUTGOING_INTERFACE_NODE_LABEL {id: \$outId}) " +
                                "MERGE (ie)-[:$MATCHED_BY_RELATIONSHIP_LABEL]->(oi)"
                    ).bind(expectation.id.toString()).to("ieId")
                        .bind(outgoingInterface.id.toString()).to("outId")
                        .run()
                    result.add(IndirectOutgoingInterfaceExpectationId(expectation.id) to OutgoingInterfaceId(outgoingInterface.id))
                }
            }
        }
        return result
    }

    override fun canHandle(event: InterfaceExpectationAddedEvent): Boolean {
        return event.protocol == Protocol("AMQP")
    }

    override fun canHandle(event: InterfaceAddedToVersionEvent): Boolean {
        return event.protocol == "AMQP"
    }
}