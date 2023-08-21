package de.rwth.swc.interact.controller.persistence.events

import de.rwth.swc.interact.domain.ComponentInterface
import org.springframework.context.ApplicationEvent

data class InterfaceAddedEvent(val src:Any, val componentInterface: ComponentInterface) : ApplicationEvent(src)