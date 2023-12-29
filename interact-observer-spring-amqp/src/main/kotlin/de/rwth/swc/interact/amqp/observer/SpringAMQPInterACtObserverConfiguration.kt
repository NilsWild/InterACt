package de.rwth.swc.interact.amqp.observer

import de.rwth.swc.interact.observer.TestObserver
import jakarta.annotation.PostConstruct
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import java.net.URL

class SpringAMQPInterACtObserverConfiguration {

    @PostConstruct
    fun addObserverLatch() {
        TestObserver.beforeStoringLatch = AmqpObserverLatch
    }

    @Bean
    fun rabbitListenerContainerFactory(
        configurer: SimpleRabbitListenerContainerFactoryConfigurer,
        connectionFactory: ConnectionFactory?,
    ): ObserverLatchRabbitListenerContainerFactory {
        val factory = ObserverLatchRabbitListenerContainerFactory()
        configurer.configure(factory, connectionFactory)
        return factory
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template =  RabbitTemplate(connectionFactory)
        template.addBeforePublishPostProcessors(ObserverLatchMessagePostProcessor())
        return template
    }

    @Bean
    fun rabbitTestTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template =  RabbitTemplate(connectionFactory)
        template.addBeforePublishPostProcessors(MessageDropperPostProcessor())
        template.addAfterReceivePostProcessors(MessageDropperPostProcessor())
        return template
    }


    @Bean
    fun observer(environment: Environment): SpringAMQPInterACtObserverService {
        return SpringAMQPInterACtObserverService(
            URL(environment.getRequiredProperty("spring.rabbitmq.httpUrl")),
            environment.getRequiredProperty("spring.rabbitmq.username"),
            environment.getRequiredProperty("spring.rabbitmq.password")
            )
    }

    @Bean
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
        traceTopic: TopicExchange,
        observeQueue: Queue
    ): Binding {
        return BindingBuilder.bind(observeQueue)
            .to(traceTopic)
            .with("#")
    }

}
