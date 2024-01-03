package de.rwth.swc.interact.amqp.observer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer

class RabbitObserverLatchCustomizer : RabbitTemplateCustomizer {
    override fun customize(template: RabbitTemplate) {
        template.addBeforePublishPostProcessors(ObserverLatchMessagePostProcessor())
    }
}