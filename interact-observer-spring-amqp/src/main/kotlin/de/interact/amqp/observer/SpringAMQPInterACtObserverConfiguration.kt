package de.interact.amqp.observer

import de.interact.amqp.TestAmqpClient
import de.interact.domain.testobservation.service.TestObservationManager
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment

class SpringAMQPInterACtObserverConfiguration {

    @Bean
    fun amqpObserverLatch(observationManager: TestObservationManager): AmqpObserverLatch {
        observationManager.messageObservers.add(AmqpObserverLatch)
        return AmqpObserverLatch
    }

    @Bean
    @Order(LOWEST_PRECEDENCE - 1)
    fun rabbitTemplate(
        configurer: RabbitTemplateConfigurer,
        connectionFactory: ConnectionFactory,
        customizers: ObjectProvider<RabbitTemplateCustomizer>
    ): RabbitTemplate {
        val template = RabbitTemplate()
        template.setObservationEnabled(true)
        configurer.configure(template, connectionFactory)
        customizers.forEach { customizer -> customizer.customize(template) }
        template.addBeforePublishPostProcessors(ObservationPublishPostProcessor())
        return template
    }

    @Bean
    @Order(LOWEST_PRECEDENCE)
    @Qualifier("rabbitTestTemplate")
    fun rabbitTestTemplate(
        configurer: RabbitTemplateConfigurer,
        connectionFactory: ConnectionFactory,
        customizers: ObjectProvider<RabbitTemplateCustomizer>
    ): RabbitTemplate {
        val template = RabbitTemplate()
        template.setObservationEnabled(true)
        configurer.configure(template, connectionFactory)
        customizers.forEach { customizer -> customizer.customize(template) }
        template.addBeforePublishPostProcessors(TestClientPublishPostProcessor())
        return template
    }


    @Bean
    fun testAmqpClient(
        @Qualifier("rabbitTestTemplate")
        rabbitTemplate: RabbitTemplate
    ): TestAmqpClient {
        return TestAmqpClient(rabbitTemplate)
    }

    @Bean
    fun observer(environment: Environment): SpringAMQPInterACtObserverService {
        return SpringAMQPInterACtObserverService(
            environment.getRequiredProperty("spring.rabbitmq.httpUrl"),
            environment.getRequiredProperty("spring.rabbitmq.username"),
            environment.getRequiredProperty("spring.rabbitmq.password")
        )
    }

    @Bean
    @Qualifier("traceTopic")
    fun traceTopic(): TopicExchange {
        val topicExchange = TopicExchange("amq.rabbitmq.trace")
        topicExchange.isInternal = true // necessary to match the exchange attributes since this exchange already exists
        return topicExchange
    }

    @Bean
    fun observeQueue(): Queue {
        return Queue("observe_queue")
    }

    @Bean
    fun bindingObserve(
        @Qualifier("traceTopic")
        traceTopic: TopicExchange,
        observeQueue: Queue
    ): Binding {
        return BindingBuilder.bind(observeQueue)
            .to(traceTopic)
            .with("#")
    }

}
