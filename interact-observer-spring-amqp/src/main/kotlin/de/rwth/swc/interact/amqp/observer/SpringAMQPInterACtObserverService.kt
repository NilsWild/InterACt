package de.rwth.swc.interact.amqp.observer

import com.rabbitmq.http.client.Client
import com.rabbitmq.http.client.domain.ExchangeInfo
import de.rwth.swc.interact.amqp.StringAMQPMessage
import de.rwth.swc.interact.domain.*
import de.rwth.swc.interact.domain.amqp.QueueBinding
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.utils.Logging
import de.rwth.swc.interact.utils.logger
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import java.net.URL
import java.nio.charset.StandardCharsets

class SpringAMQPInterACtObserverService(rabbitUrl: String, rabbitUser: String, rabbitPassword: String) : Logging {

    private val log = logger()
    private var lastRecordedMessage: de.rwth.swc.interact.domain.Message? = null
    private val rabbitClient = Client(URL("$rabbitUrl/api"), rabbitUser, rabbitPassword)

    @RabbitListener(queues = ["observe_queue"])
    private fun observe(message: Message) {
        if (!MessageDropper.shouldMessageBeDropped()) {
            if (message.messageProperties.receivedRoutingKey.startsWith("publish."))
                observePublish(message)
            else if (message.messageProperties.receivedRoutingKey.startsWith("deliver."))
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
        val messageHeaders = (message.messageProperties.headers["properties"] as HashMap<String, Any>)["headers"] as HashMap<String, Any>
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
            "\"${String(message.body, StandardCharsets.UTF_8)}\""
        )

        val sentMessage = SentMessage(
            MessageType.Sent.COMPONENT_RESPONSE,
            MessageValue(SerializationConstants.mapper.writeValueAsString(payload)),
            OutgoingInterface(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("exchangeType", exchangeType),
                        Pair("exchangeName", exchange),
                        Pair("routingKey", routingKey),
                        Pair("headers", SerializationConstants.mapper.writeValueAsString(routingHeaders).replace("\"", "'"))
                    )
                )
            )
        )
        TestObserver.recordMessage(sentMessage)
    }

    private fun observeDeliver(message: Message) {
        val queue = message.messageProperties.receivedRoutingKey.substring("deliver.".length)
        val messageHeaders = (message.messageProperties.headers["properties"] as HashMap<String, Any>)["headers"] as HashMap<String, Any>
        val payload = StringAMQPMessage(
            messageHeaders,
            "\"${String(message.body, StandardCharsets.UTF_8)}\""
        )
        val bindings = arrayListOf<QueueBinding>()
        for (bindingInfo in rabbitClient.getQueueBindings("/", queue).filter { it.source != "" }) {
            bindings.add(QueueBinding(bindingInfo.source, bindingInfo.routingKey, bindingInfo.arguments.mapValues { it.value.toString() }))
        }

        val receivedMessage = ReceivedMessage(
            MessageType.Received.STIMULUS,
            MessageValue(SerializationConstants.mapper.writeValueAsString(payload)),
            IncomingInterface(
                Protocol("AMQP"),
                ProtocolData(
                    mapOf(
                        Pair("queueName", queue),
                        Pair("queueBindings", SerializationConstants.mapper.writeValueAsString(bindings))
                    )
                )
            )
        )
        TestObserver.recordMessage(receivedMessage)
    }



}
