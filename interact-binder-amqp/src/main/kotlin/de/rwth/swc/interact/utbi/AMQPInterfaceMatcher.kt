package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.domain.IncomingInterface
import de.rwth.swc.interact.domain.OutgoingInterface
import de.rwth.swc.interact.domain.amqp.ExchangeType
import de.rwth.swc.interact.domain.amqp.toAmqpData
import org.springframework.stereotype.Component

@Component
class AMQPInterfaceMatcher(
    private val topicExchangeMatcher: TopicExchangeMatcher,
    private val headerExchangeMatcher: HeaderExchangeMatcher
) {

    fun matchAMQPInterfaces(outgoingInterface: OutgoingInterface, incomingInterface: IncomingInterface): Boolean {
        if (!(outgoingInterface.protocol.toString() == "AMQP" && incomingInterface.protocol.toString() == "AMQP")) {
            return false
        }

        val incomingInterfaceProtocolData = incomingInterface.protocolData.toAmqpData()
        val outgoingInterfaceProtocolData = outgoingInterface.protocolData.toAmqpData()

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
