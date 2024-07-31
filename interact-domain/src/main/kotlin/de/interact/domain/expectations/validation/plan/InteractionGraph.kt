package de.interact.domain.expectations.validation.plan

import com.fasterxml.uuid.Generators
import de.interact.domain.expectations.validation.test.Test
import de.interact.domain.expectations.validation.test.isEqualTo
import de.interact.domain.shared.Entity
import de.interact.domain.shared.InteractionGraphId
import de.interact.domain.shared.InterfaceId
import de.interact.domain.shared.hashedSha256
import java.util.*
import java.util.function.Predicate

data class InteractionGraph(
    val interactions: Set<Interaction> = emptySet(),
    val adjacencyMap: Map<Interaction, Set<Interaction>> = emptyMap(),
    val reverseAdjacencyMap: Map<Interaction, Set<Interaction>> = emptyMap(),
    override val version: Long? = null
):  Entity<InteractionGraphId>() {
    override val id: InteractionGraphId = InteractionGraphId(
        if(source != null) Generators.nameBasedGenerator().generate(hash(source!!)) else UUID.randomUUID()
    )
}

val InteractionGraph.sinks: Set<Interaction> get() = interactions.filter { adjacencyMap[it]!!.isEmpty() }.toSet()

val InteractionGraph.source: Interaction? get() = interactions.firstOrNull { reverseAdjacencyMap[it]!!.isEmpty() }

private fun InteractionGraph.hash(interaction: Interaction) : String {
    //TODO switch to testcase hash
    return if(adjacencyMap[interaction]!!.isEmpty()) {
        hashedSha256(interaction.from.sortedBy { it.id.value }, interaction.to.sortedBy { it.id.value })
    } else {
        hashedSha256(interaction.from.sortedBy { it.id.value }, interaction.to.sortedBy { it.id.value }, adjacencyMap[interaction]!!.map { hash(it) }.sorted())
    }
}

fun InteractionGraph.leadsTo(interfaceIds: Set<InterfaceId>): Boolean {
    return findInteractionsThatLeadTo(interfaceIds).size == interfaceIds.size
}

fun InteractionGraph.leadsTo(interfaceId: InterfaceId): Boolean {
    return findInteractionsThatLeadTo(setOf(interfaceId)).isNotEmpty()
}

private fun InteractionGraph.findInteractionsThatLeadTo(interfaceIds: Set<InterfaceId>): Set<Interaction> {
    val toTraverse: Queue<Interaction> = LinkedList()
    toTraverse.addAll(sinks)
    val interactionsThatLeadTo = mutableSetOf<Interaction>()
    while (toTraverse.isNotEmpty()) {
        val current = toTraverse.poll()
        if (current.from.map { it.id }.containsAll(interfaceIds) || current.to.map { it.id }.containsAll(interfaceIds)) {
            interactionsThatLeadTo.add(current)
        }
        toTraverse.addAll(reverseAdjacencyMap[current]!!)
    }
    return interactionsThatLeadTo
}

fun InteractionGraph.removeUnnecessaryInteractionsToReach(interfaceIds: Set<InterfaceId>): InteractionGraph {
    val toTraverse: Queue<Interaction> = LinkedList()
    toTraverse.addAll(findInteractionsThatLeadTo(interfaceIds))
    val interactionsToKeep = mutableSetOf<Interaction>()
    while (toTraverse.isNotEmpty()) {
        val current = toTraverse.poll()
        interactionsToKeep.add(current)
        toTraverse.addAll(reverseAdjacencyMap[current]!!)
    }
    return this.copy(
        interactions = interactionsToKeep,
        adjacencyMap = adjacencyMap.filterKeys { interactionsToKeep.contains(it) }.mapValues { (_,v) -> v.filter{interactionsToKeep.contains(it)}.toSet()},
        reverseAdjacencyMap = reverseAdjacencyMap.filterKeys { interactionsToKeep.contains(it) }.mapValues { (_, v) -> v.filter{interactionsToKeep.contains(it)}.toSet() }
    )
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

    return this.replaceInteractions(replacements)
}

fun InteractionGraph.replaceInteractions(replacements: Map<Interaction, Interaction>): InteractionGraph {
    val newInteractions = interactions.map { replacements[it]!! }.toSet()
    val newAdjacencyMap = adjacencyMap.map { (k, v) -> replacements[k]!! to v.map { replacements[it]!! }.toSet() }.toMap()
    val newReverseAdjacencyMap = reverseAdjacencyMap.map { (k, v) -> replacements[k]!! to v.map { replacements[it]!! }.toSet() }.toMap()

    return this.copy(
        interactions = newInteractions,
        adjacencyMap = newAdjacencyMap,
        reverseAdjacencyMap = newReverseAdjacencyMap
    )
}

fun InteractionGraph.findFirstInteractionTraversingReverseAdjacencyMap(start: Interaction, predicate: Predicate<Interaction>): Interaction?{
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
    val newAdjacencyMap = adjacencyMap +
        prevInteractions.map { it to (adjacencyMap[it] ?: emptySet()) + newInteraction }
            .toMap() + (newInteraction to emptySet())
    val newReverseAdjacencyMap = mapOf(newInteraction to prevInteractions) + newAdjacencyMap.entries.fold(reverseAdjacencyMap) { acc, (k, v) ->
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
