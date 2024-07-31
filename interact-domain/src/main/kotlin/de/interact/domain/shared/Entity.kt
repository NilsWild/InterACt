package de.interact.domain.shared

import java.util.*

abstract class Entity<out ID:Any>{
    abstract val id: ID
    abstract val version: Long?

    fun toEntityReference(): EntityReference<ID> {
        return EntityReference(this)
    }
}

data class EntityReference<out ID:Any>(val id: ID, val version: Long?) {
    constructor(entity: Entity<ID>): this(entity.id, entity.version)

    override fun equals(entity: Any?): Boolean {
        return when(entity) {
            is EntityReference<*> -> id == entity.id
            is Entity<*> -> id == entity.id
            else -> false
        }
    }
}

interface EntityReferenceProjection {
    val id: UUID
    val version: Long?
}

interface EntityReferenceWithLabelsProjection : EntityReferenceProjection {
    val labels: Set<String>
}