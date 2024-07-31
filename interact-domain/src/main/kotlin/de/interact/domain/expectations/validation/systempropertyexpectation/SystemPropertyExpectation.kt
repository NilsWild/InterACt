package de.interact.domain.expectations.validation.systempropertyexpectation

import com.fasterxml.uuid.Generators
import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.SystemPropertyExpectationId
import de.interact.domain.shared.SystemPropertyExpectationIdentifier

sealed class SystemPropertyExpectation{
    abstract val identifier: SystemPropertyExpectationIdentifier
    abstract val fromInterface: InterfaceExpectation.IncomingInterfaceExpectation
    abstract val toInterface: InterfaceExpectation.OutgoingInterfaceExpectation
    abstract val systemExpectations: Set<SystemInteractionExpectationId>

    data class NewSystemPropertyExpectation(
        override val identifier: SystemPropertyExpectationIdentifier,
        override val fromInterface: InterfaceExpectation.IncomingInterfaceExpectation,
        override val toInterface: InterfaceExpectation.OutgoingInterfaceExpectation
    ) : SystemPropertyExpectation() {
        override val systemExpectations: Set<SystemInteractionExpectationId> = emptySet()
    }

    data class PendingSystemPropertyExpectation(
        override val identifier: SystemPropertyExpectationIdentifier,
        override val fromInterface: InterfaceExpectation.IncomingInterfaceExpectation,
        override val toInterface: InterfaceExpectation.OutgoingInterfaceExpectation,
        override val systemExpectations: Set<SystemInteractionExpectationId>
    ) : SystemPropertyExpectation()

    data class FailedSystemPropertyExpectation(
        override val identifier: SystemPropertyExpectationIdentifier,
        override val fromInterface: InterfaceExpectation.IncomingInterfaceExpectation,
        override val toInterface: InterfaceExpectation.OutgoingInterfaceExpectation,
        override val systemExpectations: Set<SystemInteractionExpectationId> = emptySet()
    ) : SystemPropertyExpectation()

    data class ValidatedSystemPropertyExpectation(
        override val identifier: SystemPropertyExpectationIdentifier,
        override val fromInterface: InterfaceExpectation.IncomingInterfaceExpectation,
        override val toInterface: InterfaceExpectation.OutgoingInterfaceExpectation,
        override val systemExpectations: Set<SystemInteractionExpectationId> = emptySet()
    ) : SystemPropertyExpectation()
}

val SystemPropertyExpectation.status: SystemPropertyExpectationStatus
    get() = when(this) {
        is SystemPropertyExpectation.NewSystemPropertyExpectation -> SystemPropertyExpectationStatus.NEW
        is SystemPropertyExpectation.PendingSystemPropertyExpectation -> SystemPropertyExpectationStatus.EVALUATING
        is SystemPropertyExpectation.FailedSystemPropertyExpectation -> SystemPropertyExpectationStatus.FAILED
        is SystemPropertyExpectation.ValidatedSystemPropertyExpectation -> SystemPropertyExpectationStatus.VALIDATED
    }

fun SystemPropertyExpectationIdFromIdentifier(identifier: SystemPropertyExpectationIdentifier) = SystemPropertyExpectationId(
    Generators.nameBasedGenerator().generate("SystemPropertyExpectationId:$identifier")
)

