package de.rwth.swc.interact.controller.persistence.events

import de.rwth.swc.interact.domain.InterfaceExpectation
import org.springframework.context.ApplicationEvent

data class InterfaceExpectationAddedEvent(val src:Any, val interfaceExpectation: InterfaceExpectation) : ApplicationEvent(src)