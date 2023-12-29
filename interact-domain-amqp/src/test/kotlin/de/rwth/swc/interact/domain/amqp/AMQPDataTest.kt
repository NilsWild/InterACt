package de.rwth.swc.interact.domain.amqp

internal class AMQPDataTest {

    fun `AMQPData to ProtocolData and back`() {
        val amqpData = AMQPData(
            exchangeName = "exchangeName",
            exchangeType = ExchangeType.TOPIC,
            routingKey = "routingKey",
            headers = mapOf("key" to "value"),
            queueBindings = listOf(QueueBinding("source", "routingKey"))
        )
        val protocolData = amqpData.toProtocolData()
        val actual = protocolData.toAmqpData()
        assert(amqpData == actual)
    }
}