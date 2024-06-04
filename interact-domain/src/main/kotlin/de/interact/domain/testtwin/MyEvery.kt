package de.interact.domain.testtwin

import arrow.core.foldMap
import arrow.optics.Every
import arrow.typeclasses.Monoid
import java.util.SortedSet

object MyEvery {
    @JvmStatic
    fun <A> set(): Every<Set<A>, A> =
        object : Every<Set<A>, A> {
            override fun modify(source: Set<A>, map: (focus: A) -> A): Set<A> =
                source.map(map).toSet()

            override fun <R> foldMap(M: Monoid<R>, source: Set<A>, map: (focus: A) -> R): R =
                source.foldMap(M, map)
        }

    @JvmStatic
    fun <A:Comparable<A>> sortedSet(): Every<SortedSet<A>, A> =
        object : Every<SortedSet<A>, A> {
            override fun modify(source: SortedSet<A>, map: (focus: A) -> A): SortedSet<A> =
                source.map(map).toSortedSet()

            override fun <R> foldMap(M: Monoid<R>, source: SortedSet<A>, map: (focus: A) -> R): R =
                source.foldMap(M, map)
        }
}