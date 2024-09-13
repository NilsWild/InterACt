package de.interact.domain.amqp

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.interact.domain.serialization.SerializationConstants

class AmqpMessageDeserializer: StdDeserializer<AmqpMessage<*>>(AmqpMessage::class.java), ContextualDeserializer {

    private var bodyType: JavaType? = null

    override fun deserialize(parser: JsonParser, context: DeserializationContext): AmqpMessage<*> {
        val root = parser.codec.readTree<TreeNode>(parser)
        val body = root.get("body") as TextNode
        val r = AmqpMessage(
            headers = (root.get("headers") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
            body = body.asText()
        )
        val deserializedBody  = SerializationConstants.getMessageDeserializer(r).readBody(r, bodyType!!.containedType(0))
        return AmqpMessage(
            headers = r.headers,
            body = deserializedBody
        )
    }

    override fun createContextual(context: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val bodyType = property?.type ?: context.contextualType
        return AmqpMessageDeserializer().apply { this.bodyType = bodyType }
    }
}