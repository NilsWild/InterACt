package de.interact.amqp.observer

import com.rabbitmq.http.client.Client
import com.rabbitmq.http.client.domain.ExchangeInfo
import de.interact.amqp.StringAMQPMessage
import de.interact.domain.amqp.ExchangeName
import de.interact.domain.amqp.QueueBinding
import de.interact.domain.amqp.RoutingKey
import de.interact.domain.serialization.SerializationConstants
import de.interact.domain.shared.Protocol
import de.interact.domain.shared.ProtocolData
import de.interact.domain.testobservation.config.Configuration
import de.interact.domain.testobservation.model.*
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import java.net.URL
import java.nio.charset.StandardCharsets

class SpringAMQPInterACtObserverService(rabbitUrl: String, rabbitUser: String, rabbitPassword: String) {

    private val rabbitClient = Client(URL("$rabbitUrl/api"), rabbitUser, rabbitPassword)

    @RabbitListener(queues = ["observe_queue"])
    private fun observe(message: Message) {
        val messageHeaders =
            (message.messageProperties.headers["properties"] as HashMap<String, Any>)["headers"] as HashMap<String, Any>

        if (message.messageProperties.receivedRoutingKey.startsWith("publish.") && messageHeaders["interact.sender.type"].toString() == "CUT") {
            observePublish(message)
            AmqpObserverLatch.decrement()
        } else if (message.messageProperties.receivedRoutingKey.startsWith("deliver.") && messageHeaders["interact.sender.type"].toString() == "TEST") {
            observeDeliver(message)
            AmqpObserverLatch.decrement()
        }
    }

    private fun observePublish(message: Message) {
        val headers = message.messageProperties.headers
        val exchange = headers["exchange_name"].toString()
        var routingKey = headers["routing_keys"].toString()
        routingKey = routingKey.substring(1, routingKey.length - 1)
        val exchangeInfo: ExchangeInfo = rabbitClient.getExchange("/", exchange)
        val exchangeType = exchangeInfo.type

        @Suppress("UNCHECKED_CAST")
        val messageHeaders =
            (message.messageProperties.headers["properties"] as HashMap<String, Any>)["headers"] as HashMap<String, Any>
        messageHeaders.remove("traceparent")
        messageHeaders.remove("interact.sender.type")
        val routingHeaders = HashMap<Any, Any>()
        if (exchangeType == "headers") {
            for (bindingInfo in rabbitClient.getBindings("/").filter { it.source == exchange }) {
                for (routingAttribute in bindingInfo.arguments.keys) {
                    if (messageHeaders.containsKey(routingAttribute) && !routingHeaders.containsKey(routingAttribute)) {
                        routingHeaders[routingAttribute] = messageHeaders[routingAttribute] as Any
                    }
                }
            }
        }
        val payload = StringAMQPMessage(
            messageHeaders,
            String(message.body, StandardCharsets.UTF_8)
        )

        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addComponentResponse(
            MessageValue(SerializationConstants.messageMapper.writeValueAsString(payload)),
            OutgoingInterface(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("exchangeType", exchangeType.uppercase()),
                        Pair("exchangeName", exchange),
                        Pair("routingKey", routingKey),
                        Pair(
                            "headers",
                            SerializationConstants.mapper.writeValueAsString(routingHeaders).replace("\"", "'")
                        )
                    )
                )
            )
        )
    }

    private fun observeDeliver(message: Message) {
        val queue = message.messageProperties.receivedRoutingKey.substring("deliver.".length)

        @Suppress("UNCHECKED_CAST")
        val messageHeaders =
            (message.messageProperties.headers["properties"] as HashMap<String, Any>)["headers"] as HashMap<String, Any>
        messageHeaders.remove("traceparent")
        messageHeaders.remove("interact.sender.type")
        val payload = StringAMQPMessage(
            messageHeaders,
            String(message.body, StandardCharsets.UTF_8)
        )
        val bindings = arrayListOf<QueueBinding>()
        for (bindingInfo in rabbitClient.getQueueBindings("/", queue).filter { it.source != "" }) {
            bindings.add(
                QueueBinding(
                    ExchangeName(bindingInfo.source),
                    RoutingKey(bindingInfo.routingKey),
                    bindingInfo.arguments.mapValues { it.value.toString() })
            )
        }

        val messageValue = MessageValue(SerializationConstants.messageMapper.writeValueAsString(payload))
        val incomingInterface = IncomingInterface(
            Protocol("AMQP"),
            ProtocolData(
                mapOf(
                    Pair("queueName", queue),
                    Pair("queueBindings", SerializationConstants.mapper.writeValueAsString(bindings))
                )
            )
        )

        Configuration.observationManager!!.getCurrentTestCase().observedBehavior.addStimulus(
            messageValue,
            incomingInterface
        )
    }


}
