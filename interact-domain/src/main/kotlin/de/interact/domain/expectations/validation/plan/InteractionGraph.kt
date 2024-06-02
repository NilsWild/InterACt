package de.interact.domain.expectations.validation.plan

import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.expectations.validation.test.isEqualTo
import de.interact.domain.shared.Entity
import de.interact.domain.shared.InteractionGraphId
import de.interact.domain.shared.InterfaceId
import java.util.*
import java.util.function.Predicate

data class InteractionGraph(
    val interactions: Set<Interaction> = emptySet(),
    val adjacencyMap: Map<Interaction, Set<Interaction>> = emptyMap(),
    val reverseAdjacencyMap: Map<Interaction, Set<Interaction>> = emptyMap(),
    override val id: InteractionGraphId = InteractionGraphId(UUID.randomUUID()),
    override val version: Long? = null
):  Entity<InteractionGraphId>()

val InteractionGraph.sinks: Set<Interaction> get() = interactions.filter { adjacencyMap[it]!!.isEmpty() }.toSet()

fun InteractionGraph.leadsTo(interfaceIds: Set<InterfaceId>): Boolean {
    return interfaceIds.all { leadsTo(it) }
}

fun InteractionGraph.leadsTo(interfaceId: InterfaceId): Boolean {
    val toTraverse: Queue<Interaction> = LinkedList()
    toTraverse.addAll(sinks)
    while (toTraverse.isNotEmpty()) {
        val current = toTraverse.remove()
        if (current.to.map { it.id }.contains(interfaceId)) {
            return true
        }
        toTraverse.addAll(reverseAdjacencyMap[current]!!)
    }
    return false
}

internal fun InteractionGraph.handle(test: Test): InteractionGraph {
    val validatedInteractions = interactions.filterIsInstance<Interaction.Executable>().filter { test.isEqualTo(it.testCase) }

    val replacements = interactions.map {
        if (it is Interaction.Executable && validatedInteractions.contains(it)) {
            it to it.validate(test)
        } else {
            it to it
        }
    }.toMap()

    val newGraph = this.replaceInteractions(replacements)

    return newGraph.updateInteractions()
}

private fun InteractionGraph.replaceInteractions(replacements: Map<Interaction, Interaction>): InteractionGraph {
    val newInteractions = interactions.map { replacements[it]!! }.toSet()
    val newAdjacencyMap = adjacencyMap.map { (k, v) -> replacements[k]!! to v.map { replacements[it]!! }.toSet() }.toMap()
    val newReverseAdjacencyMap = newAdjacencyMap.entries.fold(reverseAdjacencyMap) { acc, (k, v) ->
        v.fold(acc) { acc2, it ->
            acc2 + (it to (acc[it] ?: emptySet()) + k)
        }
    }

    return this.copy(
        interactions = newInteractions,
        adjacencyMap = newAdjacencyMap,
        reverseAdjacencyMap = newReverseAdjacencyMap
    )
}

private fun InteractionGraph.updateInteractions(): InteractionGraph {
    val nextInteractionsToTest = interactions.filterIsInstance<Interaction.Finished.Validated>().flatMap {
        adjacencyMap[it]!!
    }.filterIsInstance<Interaction.Pending>().filter {
        !reverseAdjacencyMap[it]!!.any { it !is Interaction.Finished.Validated }
    }

    return if (nextInteractionsToTest.isEmpty()) {
        this
    } else {
        // traverse reverseAdjacencymatrix for each nextInteractionToTest to find last occurence of TestCase. Copy the replacements and add the replacements caused by preceeding validated tests
        val replacements = interactions.map {
            if(it is Interaction.Pending && nextInteractionsToTest.contains(it)) {
                val lastManipulation = this.findFirstTraversingReverseAdjacencyMap(it){ interaction ->
                    interaction is Interaction.Finished.Validated && interaction.testCase.derivedFrom == it.testCase.deriveFrom
                }
                if(lastManipulation != null){
                    it to Interaction.Executable(
                        it.derivedFrom,
                        TestCase.ExecutableTestCase(
                            it.testCase.deriveFrom,
                            it.testCase.replacements,
                            TODO(),
                            it.testCase.id
                        ),
                        it.from,
                        it.to,
                        it.id
                    )
                } else {
                  it to Interaction.Executable(
                      it.derivedFrom,
                      TestCase.ExecutableTestCase(
                          it.testCase.deriveFrom,
                          it.testCase.replacements,
                          TODO(),
                          it.testCase.id
                      ),
                      it.from,
                      it.to,
                      it.id
                  )
                }
            } else {
                it to it
            }
        }.toMap()
        this.replaceInteractions(replacements)
    }
}

private fun InteractionGraph.findFirstTraversingReverseAdjacencyMap(start: Interaction, predicate: Predicate<Interaction>): Interaction?{
    val toTraverse: Queue<Interaction> = LinkedList()
    toTraverse.add(start)
    while(toTraverse.isNotEmpty()) {
        val current = toTraverse.remove()
        if(predicate.test(current)) {
            return current
        }
        toTraverse.addAll(reverseAdjacencyMap[current]!!)
    }
    return null
}

fun InteractionGraph.addInteraction(newInteraction: Interaction, prevInteractions: Set<Interaction> = emptySet()): InteractionGraph {
    val newInteractions = interactions + newInteraction
    val newAdjacencyMap =
        adjacencyMap + prevInteractions.map { it to (adjacencyMap[it] ?: emptySet()) + newInteraction }
            .toMap() + (newInteraction to emptySet())
    val newReverseAdjacencyMap = newAdjacencyMap.entries.fold(reverseAdjacencyMap) { acc, (k, v) ->
        v.fold(acc) { acc2, it ->
            acc2 + (it to (acc[it] ?: emptySet()) + k)
        }
    }
    return this.copy(
        interactions = newInteractions,
        adjacencyMap = newAdjacencyMap,
        reverseAdjacencyMap = newReverseAdjacencyMap
    )
}
