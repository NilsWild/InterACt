package de.interact.domain.expectations.execution.systemexpectation

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonIgnore
import de.interact.domain.shared.InterfaceSpec
import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.SystemPropertyExpectationIdentifier

sealed class SystemExpectation {
    abstract val filter: ((Any) -> Unit)
    abstract val assertions: ((Any, Any) -> Unit)

    data class SystemPropertyExpectation(
        val identifier: SystemPropertyExpectationIdentifier,
        val stimulusInterfaceExpectation: InterfaceSpec,
        val reactionInterfaceExpectation: InterfaceSpec,
        @JsonIgnore
        override val filter: ((Any) -> Unit),
        @JsonIgnore
        override val assertions: ((Any, Any) -> Unit)
    ) : SystemExpectation()

    sealed class SystemInteractionExpectation : SystemExpectation() {
        abstract val id: SystemInteractionExpectationId
        abstract val stimulus: Any
        abstract fun verify(): Either<SystemInteractionVerificationResult.SystemInteractionVerificationFailure, SystemInteractionVerificationResult.SystemInteractionVerificationSuccess>

        data class SystemInteractionExpectationCandidate(
            override val id: SystemInteractionExpectationId,
            override val stimulus: Any,
            @JsonIgnore
            override val filter: ((Any) -> Unit),
            @JsonIgnore
            override val assertions: ((Any, Any) -> Unit)
        ) : SystemInteractionExpectation() {
            override fun verify(): Either<SystemInteractionVerificationResult.SystemInteractionVerificationFailure.FilterError, SystemInteractionVerificationResult.SystemInteractionVerificationSuccess.Selected> {
                try {
                    filter(stimulus)
                    return Either.Right(SystemInteractionVerificationResult.SystemInteractionVerificationSuccess.Selected)
                } catch (e: Exception) {
                    return Either.Left(
                        SystemInteractionVerificationResult.SystemInteractionVerificationFailure.FilterError(
                            e
                        )
                    )
                }
            }
        }

        data class SimulatedSystemInteractionExpectation(
            override val id: SystemInteractionExpectationId,
            override val stimulus: Any,
            val reaction: Any,
            @JsonIgnore
            override val filter: ((Any) -> Unit),
            @JsonIgnore
            override val assertions: ((Any, Any) -> Unit)
        ) : SystemInteractionExpectation() {
            override fun verify(): Either<SystemInteractionVerificationResult.SystemInteractionVerificationFailure.AssertionError, SystemInteractionVerificationResult.SystemInteractionVerificationSuccess.Verified> {
                try {
                    assertions(stimulus, reaction)
                    return Either.Right(SystemInteractionVerificationResult.SystemInteractionVerificationSuccess.Verified)
                } catch (e: Exception) {
                    return Either.Left(
                        SystemInteractionVerificationResult.SystemInteractionVerificationFailure.AssertionError(
                            e
                        )
                    )
                }
            }
        }
    }
}