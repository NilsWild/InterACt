package de.interact.domain.shared

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
sealed interface  InteractionExpectationStatus {

    companion object {
        fun fromString(string: String): InteractionExpectationStatus {
            return when (string) {
                "NoValidatingPathFound" -> NoValidatingPathFound
                "Validating" -> Validating
                "Validated" -> Validated
                else -> throw IllegalArgumentException("Unknown interaction expectation status: $string")
            }
        }
    }

    data object NoValidatingPathFound : InteractionExpectationStatus {
        override fun toString(): String {
            return "NoValidatingPathFound"
        }
    }

    data object Validating: InteractionExpectationStatus {
        override fun toString(): String {
            return "Validating"
        }
    }

    data object Validated: InteractionExpectationStatus {
        override fun toString(): String {
            return "Validated"
        }
    }
}