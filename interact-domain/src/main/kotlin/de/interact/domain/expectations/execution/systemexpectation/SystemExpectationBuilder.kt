package de.interact.domain.expectations.execution.systemexpectation

import de.interact.domain.shared.SystemInteractionExpectationId
import de.interact.domain.shared.SystemPropertyExpectationIdentifier
import de.interact.domain.shared.InterfaceSpec

sealed class SystemExpectationBuilder {
    protected var stimulus: Any? = null
    protected var filter: ((Any) -> Unit)? = null
    protected var stimulusInterfaceSpec: InterfaceSpec? = null
    protected var reaction: Any? = null
    protected var reactionInterfaceSpec: InterfaceSpec? = null
    protected var assertions: ((Any, Any) -> Unit)? = null

    fun <T> whenAStimulus(stimulus: T?): SystemExpectationBuilder {
        this.stimulus = stimulus
        return this
    }

    fun <T : Any> thatCompliesWith(filter: (T) -> Unit): SystemExpectationBuilder {
        this.filter = filter as (Any) -> Unit
        return this
    }

    fun isSentBy(stimulusInterfaceSpec: InterfaceSpec): SystemExpectationBuilder {
        this.stimulusInterfaceSpec = stimulusInterfaceSpec
        return this
    }

    fun then(): SystemExpectationBuilder {
        return this
    }

    fun <T> aMessage(reaction: T?): SystemExpectationBuilder {
        this.reaction = reaction
        return this
    }

    fun isReceivedOn(reactionInterfaceSpec: InterfaceSpec): SystemExpectationBuilder {
        this.reactionInterfaceSpec = reactionInterfaceSpec
        return this
    }

    fun <T : Any, U : Any> asserting(assertions: (T, U) -> Unit): SystemExpectationBuilder {
        this.assertions = assertions as (Any, Any) -> Unit
        return this
    }

    abstract fun build(): SystemExpectation

    class SystemPropertyExpectationBuilder(private var identifier: SystemPropertyExpectationIdentifier) :
        SystemExpectationBuilder() {
        override fun build(): SystemExpectation.SystemPropertyExpectation {
            return SystemExpectation.SystemPropertyExpectation(
                identifier,
                stimulusInterfaceSpec!!,
                reactionInterfaceSpec!!,
                filter!!,
                assertions!!
            )
        }
    }

    class SystemInteractionExpectationBuilder(private var id: SystemInteractionExpectationId) :
        SystemExpectationBuilder() {
        override fun build(): SystemExpectation.SystemInteractionExpectation {
            if (reaction == null) {
                return SystemExpectation.SystemInteractionExpectation.SystemInteractionExpectationCandidate(
                    id,
                    stimulus!!,
                    filter!!,
                    assertions!!
                )
            } else {
                return SystemExpectation.SystemInteractionExpectation.SimulatedSystemInteractionExpectation(
                    id,
                    stimulus!!,
                    reaction!!,
                    filter!!,
                    assertions!!
                )
            }
        }
    }
}