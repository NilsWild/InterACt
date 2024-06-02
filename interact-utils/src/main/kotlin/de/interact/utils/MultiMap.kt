package de.interact.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

class MultiMap<K, V>(private val map: MutableMap<K, MutableCollection<V>> = HashMap()) {

    @get:JsonIgnore
    val size: Int
        get() {
            var size = 0
            for (value in map.values) {
                size += value.size
            }
            return size
        }

    @get:JsonIgnore
    val keys: Set<K>
        get() {
            return map.keys
        }

    @get:JsonIgnore
    val values: Set<V>
        get() {
            return map.values.flatten().toSet()
        }

    @get:JsonIgnore
    val entries: Set<Map.Entry<K, V>>
        get() {
            val hashSet = HashSet<Map.Entry<K, V>>()
            keys.forEach { key ->
                map[key]?.forEach { value ->
                    hashSet.add(AbstractMap.SimpleEntry(key, value))
                }
            }
            return hashSet
        }

    operator fun get(key: K): Collection<V> {
        return map[key] ?: hashSetOf()
    }

    fun put(key: K, value: V) {
        if (map[key] == null) {
            map[key] = HashSet()
        }
        map[key]?.add(value)
    }

    fun putAll(key: K, values: Collection<V>) {
        if (!map.containsKey(key)) {
            map[key] = HashSet()
        }
        map[key]?.addAll(values)
    }

    fun remove(key: K, value: V): Boolean {
        return map[key]?.remove(value) ?: false
    }

    fun removeAll(key: K) {
        map[key] = HashSet()
    }

    fun clear(key: K) {
        map.remove(key)
    }

    fun replace(key: K, oldValue: V, newValue: V): Boolean {
        return if (remove(key, oldValue)) {
            put(key, newValue)
            true
        } else {
            false
        }
    }

    fun replaceAll(key: K, values: Collection<V>) {
        map[key] = values.toHashSet()
    }

    fun clear() {
        map.clear()
    }

    fun containsKey(key: K): Boolean {
        return map.containsKey(key)
    }

    fun containsValue(value: V): Boolean {
        return map.values.flatten().contains(value)
    }

    @JsonIgnore
    fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    fun getMap(): Map<K, Collection<V>> {
        return map
    }

}