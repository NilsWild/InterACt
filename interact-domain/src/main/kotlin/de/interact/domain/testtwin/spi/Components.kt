package de.interact.domain.testtwin.spi

import de.interact.domain.shared.ComponentId
import de.interact.domain.testtwin.Component

interface Components {
    infix fun `find by id`(id: ComponentId): Component?
    infix fun add(component: Component): Component
    fun all(): List<Component>
}