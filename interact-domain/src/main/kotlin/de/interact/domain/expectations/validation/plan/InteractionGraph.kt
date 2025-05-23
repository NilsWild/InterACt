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
        hashedSha256(interaction.from.sortedBy { it.second.id.value }, interaction.to.sortedBy { it.second.id.value })
    } else {
        hashedSha256(interaction.from.sortedBy { it.second.id.value }, interaction.to.sortedBy { it.second.id.value }, adjacencyMap[interaction]!!.map { hash(it) }.sorted())
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
        if (current.from.map { it.first.id }.containsAll(interfaceIds) || current.to.map { it.first.id }.containsAll(interfaceIds)) {
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
    val completeAdjacencyMap = buildMap<Interaction, Set<Interaction>> {
        for (i in interactionsToKeep) {
            put(i, adjacencyMap[i]?.filter { it in interactionsToKeep }?.toSet() ?: emptySet())
        }
    }
    val completeReverseAdjacencyMap = buildMap<Interaction, Set<Interaction>> {
        for (i in interactionsToKeep) {
            put(i, reverseAdjacencyMap[i]?.filter { it in interactionsToKeep }?.toSet() ?: emptySet())
        }
    }

    return this.copy(
        interactions = interactionsToKeep,
        adjacencyMap = completeAdjacencyMap,
        reverseAdjacencyMap = completeReverseAdjacencyMap
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
    require(replacements.keys.containsAll(interactions)) {
        "All interactions must have a replacement"
    }

    val newInteractions = interactions.map { replacements[it]!! }.toSet()

    val newAdjacencyMap = buildMap<Interaction, Set<Interaction>> {
        for ((k, v) in adjacencyMap) {
            val newK = replacements[k]!!
            val newV = v.map { replacements[it]!! }.toSet()
            put(newK, getOrDefault(newK, emptySet()) + newV)
        }
        for (i in newInteractions) {
            putIfAbsent(i, emptySet())
        }
    }

    val newReverseAdjacencyMap = buildMap<Interaction, Set<Interaction>> {
        for ((from, tos) in newAdjacencyMap) {
            for (to in tos) {
                put(to, getOrDefault(to, emptySet()) + from)
            }
        }
        for (i in newInteractions) {
            putIfAbsent(i, emptySet())
        }
    }

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

fun InteractionGraph.findAllInteractionTraversingReverseAdjacencyMap(start: Interaction, predicate: Predicate<Interaction>): Set<Interaction>{
    val toTraverse: Queue<Interaction> = LinkedList()
    val result = mutableSetOf<Interaction>()
    toTraverse.add(start)
    while(toTraverse.isNotEmpty()) {
        val current = toTraverse.remove()
        if(predicate.test(current)) {
            result.add(current)
        }
        toTraverse.addAll(reverseAdjacencyMap[current]!!)
    }
    return result
}

fun InteractionGraph.addInteraction(newInteraction: Interaction, prevInteractions: Set<Interaction> = emptySet()): InteractionGraph {
    val newInteractions = interactions + newInteraction

    val newAdjacencyMap = buildMap<Interaction, Set<Interaction>> {
        // Start with existing adjacency
        putAll(adjacencyMap)
        // Ensure all interactions are keys (even if no successors)
        for (i in newInteractions) putIfAbsent(i, adjacencyMap[i] ?: emptySet())
        // Add edges from previous interactions to the new one
        for (prev in prevInteractions) {
            put(prev, (get(prev) ?: emptySet()) + newInteraction)
        }
        // Ensure the newInteraction has an empty entry
        putIfAbsent(newInteraction, get(newInteraction) ?: emptySet())
    }

    val newReverseAdjacencyMap = buildMap<Interaction, Set<Interaction>> {
        // Invert the adjacencyMap
        for ((from, tos) in newAdjacencyMap) {
            for (to in tos) {
                put(to, (get(to) ?: emptySet()) + from)
            }
        }
        // Ensure every interaction is present
        for (i in newInteractions) {
            putIfAbsent(i, get(i) ?: emptySet())
        }
    }

    return this.copy(
        interactions = newInteractions,
        adjacencyMap = newAdjacencyMap,
        reverseAdjacencyMap = newReverseAdjacencyMap
    )
}

