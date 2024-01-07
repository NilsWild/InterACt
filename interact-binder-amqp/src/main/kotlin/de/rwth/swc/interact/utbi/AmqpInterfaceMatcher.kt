package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.domain.ProtocolData
import de.rwth.swc.interact.domain.amqp.AmqpData
import de.rwth.swc.interact.domain.amqp.ExchangeType
import de.rwth.swc.interact.domain.amqp.QueueBinding
import de.rwth.swc.interact.domain.amqp.toAmqpData
import de.rwth.swc.interact.domain.serialization.SerializationConstants
import org.springframework.stereotype.Component

@Component
class AmqpInterfaceMatcher(
    private val topicExchangeMatcher: TopicExchangeMatcher,
    private val headerExchangeMatcher: HeaderExchangeMatcher
) {

    fun matchAMQPInterfaces(outgoingInterface: ProtocolData, incomingInterface: ProtocolData): Boolean {

        val incomingInterfaceProtocolData: AmqpData = incomingInterface.toAmqpData()
        val outgoingInterfaceProtocolData: AmqpData = outgoingInterface.toAmqpData()

        for (queueBinding in incomingInterfaceProtocolData.queueBindings) {
            if (outgoingInterfaceProtocolData.exchangeName == queueBinding.source) {
                when (outgoingInterfaceProtocolData.exchangeType!!) {
                    ExchangeType.DIRECT ->
                        if (outgoingInterfaceProtocolData.routingKey == queueBinding.routingKey)
                            return true

                    ExchangeType.FANOUT ->
                        return true

                    ExchangeType.TOPIC ->
                        if (topicExchangeMatcher.match(
                                outgoingInterfaceProtocolData.routingKey!!,
                                queueBinding.routingKey
                            )
                        )
                            return true

                    ExchangeType.HEADERS ->
                        if (headerExchangeMatcher.match(
                                outgoingInterfaceProtocolData.headers,
                                queueBinding.arguments
                            )
                        )
                            return true
                }
            }
        }
        return false
    }
}
