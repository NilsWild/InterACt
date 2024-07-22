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

    fun equals(entity: Entity<Any>): Boolean {
        return entity.id::class == id::class && id == entity.id && version == entity.version
    }
}

interface EntityReferenceProjection {
    val id: UUID
    val version: Long?
}

interface EntityReferenceWithLabelsProjection : EntityReferenceProjection {
    val labels: Set<String>
}