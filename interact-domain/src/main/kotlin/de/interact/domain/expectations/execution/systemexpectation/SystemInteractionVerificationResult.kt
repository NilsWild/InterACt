package de.interact.domain.expectations.execution.systemexpectation

sealed class SystemInteractionVerificationResult {
    sealed class SystemInteractionVerificationSuccess : SystemInteractionVerificationResult() {
        data object Selected : SystemInteractionVerificationSuccess()
        data object Verified : SystemInteractionVerificationSuccess()
    }

    sealed class SystemInteractionVerificationFailure : SystemInteractionVerificationResult() {
        abstract val error: Throwable

        data class FilterError(override val error: Throwable) : SystemInteractionVerificationFailure()
        data class AssertionError(override val error: Throwable) : SystemInteractionVerificationFailure()
    }
}