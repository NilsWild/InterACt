package de.interact

import de.interact.domain.expectations.specification.events.InterfaceExpectationAddedEvent
import de.interact.domain.shared.*
import de.interact.domain.testtwin.api.event.IncomingInterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.InterfaceAddedToVersionEvent
import de.interact.domain.testtwin.api.event.OutgoingInterfaceAddedToVersionEvent
import de.interact.repository.*
import de.interact.utbi.InterfaceExpectationMatcher
import org.springframework.stereotype.Service

@Service
class RestInterfaceExpectationMatcher(
    private val restInterfaceDao: RestInterfaceDao,
    private val restInterfaceExpectationsDao: RestInterfaceExpectationsDao,
    private val restInterfaceMatcher: RestInterfaceMatcher
) : InterfaceExpectationMatcher {

    override fun canHandle(event: InterfaceExpectationAddedEvent): Boolean {
        return event.protocol == Protocol("REST")
    }

    override fun canHandle(event: InterfaceAddedToVersionEvent): Boolean {
        return event.protocol == "REST"
    }

    override fun match(event: InterfaceExpectationAddedEvent): List<Pair<InterfaceExpectationId, InterfaceId>> {
        return when (event) {
            is InterfaceExpectationAddedEvent.IncomingInterfaceExpectationAddedEvent -> matchIncomingInterfaces(
                listOf(
                    restInterfaceExpectationsDao.findIncomingById(event.id)!!
                ),
                restInterfaceDao.findAllIncoming()
            )

            is InterfaceExpectationAddedEvent.OutgoingInterfaceExpectationAddedEvent -> matchOutgoingInterfaces(
                listOf(
                    restInterfaceExpectationsDao.findOutgoingById(event.id)!!
                ),
                restInterfaceDao.findAllOutgoing()
            )
        }
    }

    override fun match(event: InterfaceAddedToVersionEvent): List<Pair<InterfaceExpectationId, InterfaceId>> {
        return when (event) {
            is IncomingInterfaceAddedToVersionEvent -> matchIncomingInterfaces(
                restInterfaceExpectationsDao.findAllIncoming(),
                listOf(
                    restInterfaceDao.findIncomingById(event.interfaceId)!!
                )
            )

            is OutgoingInterfaceAddedToVersionEvent -> matchOutgoingInterfaces(
                restInterfaceExpectationsDao.findAllOutgoing(),
                listOf(
                    restInterfaceDao.findOutgoingById(event.interfaceId)!!
                )
            )
        }
    }


    private fun matchIncomingInterfaces(
        incomingExpectations: Collection<IncomingRestInterfaceExpectationProjection>,
        incomingInterfaces: Collection<IncomingRestInterfaceProjection>
    ): List<Pair<InterfaceExpectationId, InterfaceId>> {
        val result = mutableListOf<Pair<InterfaceExpectationId, InterfaceId>>()
        incomingExpectations.forEach { expectation ->
            incomingInterfaces.forEach { incomingInterface ->
                if (restInterfaceMatcher.matches(
                        incomingInterface.toDomain(),
                        expectation.toDomain()
                    )
                ) {
                    restInterfaceDao.match(incomingInterface, expectation)
                    result.add(IndirectIncomingInterfaceExpectationId(expectation.id) to IncomingInterfaceId(incomingInterface.id))
                }
            }
        }
        return result
    }

    private fun matchOutgoingInterfaces(
        outgoingExpectations: Collection<OutgoingRestInterfaceExpectationProjection>,
        outgoingInterfaces: Collection<OutgoingRestInterfaceProjection>
    ): List<Pair<InterfaceExpectationId, InterfaceId>> {
        val result = mutableListOf<Pair<InterfaceExpectationId, InterfaceId>>()
        outgoingExpectations.forEach { expectation ->
            outgoingInterfaces.forEach { outgoingInterface ->
                if (restInterfaceMatcher.matches(
                        outgoingInterface.toDomain(),
                        expectation.toDomain()
                    )
                ) {
                    restInterfaceDao.match(outgoingInterface, expectation)
                    result.add(IndirectOutgoingInterfaceExpectationId(expectation.id) to OutgoingInterfaceId(outgoingInterface.id))
                }
            }
        }
        return result
    }
}