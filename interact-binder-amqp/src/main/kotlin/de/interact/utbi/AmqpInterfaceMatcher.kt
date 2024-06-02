package de.interact.utbi

import de.interact.domain.amqp.ExchangeType
import de.interact.domain.amqp.IncomingAmqpProtocolData
import de.interact.domain.amqp.OutgoingAmqpProtocolData
import org.springframework.stereotype.Component

@Component
class AmqpInterfaceMatcher(
    private val topicExchangeMatcher: TopicExchangeMatcher,
    private val headerExchangeMatcher: HeaderExchangeMatcher
) {

    fun matchAMQPInterfaces(
        outgoingInterfaceProtocolData: OutgoingAmqpProtocolData,
        incomingInterfaceProtocolData: IncomingAmqpProtocolData
    ): Boolean {

        for (queueBinding in incomingInterfaceProtocolData.queueBindings) {
            if (outgoingInterfaceProtocolData.exchangeName == queueBinding.source) {
                when (outgoingInterfaceProtocolData.exchangeType) {
                    ExchangeType.DIRECT ->
                        if (outgoingInterfaceProtocolData.routingKey == queueBinding.routingKey)
                            return true

                    ExchangeType.FANOUT ->
                        return true

                    ExchangeType.TOPIC ->
                        if (topicExchangeMatcher.match(
                                outgoingInterfaceProtocolData.routingKey,
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
