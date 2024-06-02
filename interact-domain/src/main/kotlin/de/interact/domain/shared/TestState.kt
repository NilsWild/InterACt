package de.interact.domain.shared

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
sealed interface TestState {
    companion object {
        fun fromString(string: String): TestState {
            return when (string) {
                "Skipped" -> TestFinishedState.Skipped
                "Succeeded" -> TestFinishedState.Succeeded
                "AssertionFailed" -> TestFinishedState.Failed.AssertionFailed
                "ExceptionFailed" -> TestFinishedState.Failed.ExceptionFailed
                "NotExecuted" -> NotExecuted
                else -> throw IllegalArgumentException("Unknown test state: $string")
            }
        }
    }

    sealed interface TestFinishedState : TestState {
        data object Skipped : TestFinishedState {
            override fun toString(): String {
                return "Skipped"
            }
        }

        data object Succeeded : TestFinishedState {
            override fun toString(): String {
                return "Succeeded"
            }
        }

        sealed interface Failed : TestFinishedState {
            data object AssertionFailed : Failed {
                override fun toString(): String {
                    return "AssertionFailed"
                }
            }

            data object ExceptionFailed : Failed {
                override fun toString(): String {
                    return "ExceptionFailed"
                }
            }
        }
    }

    data object NotExecuted : TestState {
        override fun toString(): String {
            return "NotExecuted"
        }
    }
}