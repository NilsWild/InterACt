package de.interact.domain.expectations.specification.collection

import com.fasterxml.uuid.Generators
import de.interact.domain.expectations.execution.result.SystemPropertyExpectationRecord
import de.interact.domain.expectations.specification.events.InterfaceExpectationAddedEvent
import de.interact.domain.expectations.specification.events.SpecificationEvent
import de.interact.domain.expectations.specification.events.SystemPropertyExpectationAddedEvent
import de.interact.domain.shared.*

data class ExpectationsCollection(
    val name: ExpectationsCollectionName,
    val versionName: ExpectationsCollectionVersion,
    val expectations: Set<SystemPropertyExpectation> = emptySet(),
    val postPersistEvents: Set<SpecificationEvent> = emptySet(),
    override val version: Long? = null
): Entity<ExpectationsCollectionId>() {
    override val id: ExpectationsCollectionId = ExpectationsCollectionId(Generators.nameBasedGenerator().generate(
        hashedSha256(name, versionName)
    ))
}

fun ExpectationsCollection.addSystemPropertyExpectations(
    expectations: Set<SystemPropertyExpectationRecord>
): ExpectationsCollection {
    var newExpectations = this.expectations
    var newPostPersistEvents = this.postPersistEvents
    expectations.forEach {
        if (!newExpectations.any { expectation -> expectation.identifier == it.identifier }) {
            val systemPropertyExpectationId = SystemPropertyExpectationId(Generators.nameBasedGenerator().generate(
                hashedSha256(id,it.identifier)
            ))
            val systemPropertyExpectation = SystemPropertyExpectation(
                it.identifier,
                InterfaceExpectation.IncomingInterfaceExpectation.IndirectIncomingInterfaceExpectation(
                    it.fromInterface.protocol,
                    it.fromInterface.protocolData,
                    IndirectIncomingInterfaceExpectationId(Generators.nameBasedGenerator().generate(
                        hashedSha256(id, systemPropertyExpectationId, it.fromInterface.protocol, it.fromInterface.protocolData)
                    ))
                ),
                InterfaceExpectation.OutgoingInterfaceExpectation.IndirectOutgoingInterfaceExpectation(
                    it.toInterface.protocol,
                    it.toInterface.protocolData,
                    IndirectOutgoingInterfaceExpectationId(Generators.nameBasedGenerator().generate(
                        hashedSha256(id, systemPropertyExpectationId, it.toInterface.protocol, it.toInterface.protocolData)
                    )),
                ),
                systemPropertyExpectationId
            )
            newPostPersistEvents = newPostPersistEvents.plus(
                setOf(
                    SystemPropertyExpectationAddedEvent(
                        systemPropertyExpectationId
                    ),
                    InterfaceExpectationAddedEvent.IncomingInterfaceExpectationAddedEvent.IndirectIncomingInterfaceExpectationAddedEvent(
                        systemPropertyExpectation.fromInterface.id as IndirectIncomingInterfaceExpectationId,
                        systemPropertyExpectation.fromInterface.protocol,
                        systemPropertyExpectation.fromInterface.protocolData
                    ),
                    InterfaceExpectationAddedEvent.OutgoingInterfaceExpectationAddedEvent.IndirectOutgoingInterfaceExpectationAddedEvent(
                        systemPropertyExpectation.toInterface.id as IndirectOutgoingInterfaceExpectationId,
                        systemPropertyExpectation.toInterface.protocol,
                        systemPropertyExpectation.toInterface.protocolData
                    )
                )
            )
            newExpectations = newExpectations.plus(
                systemPropertyExpectation
            )
        }
    }
    return this.copy(expectations = newExpectations, postPersistEvents = newPostPersistEvents)
}