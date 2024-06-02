package de.interact.controller.persistence.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.util.*

abstract class Entity {
    @Id
    lateinit var id: UUID
    @Version
    var version: Long? = null
}