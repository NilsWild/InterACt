package de.interact.domain.rest

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.interact.domain.serialization.SerializationConstants

class RestMessageDeserializer: StdDeserializer<RestMessage<*>>(RestMessage::class.java), ContextualDeserializer {

    private var bodyType: JavaType? = null

    override fun deserialize(parser: JsonParser, context: DeserializationContext): RestMessage<*> {
        val root = parser.codec.readTree<TreeNode>(parser)
        val type = bodyType!!.rawClass.simpleName
        val body = root.get("body")
        if(body is NullNode) {
            return when(type) {
                RestMessage.Request::class.simpleName -> {
                    RestMessage.Request(
                        path = (root.get("path") as TextNode).asText(),
                        parameters = (root.get("parameters") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        headers = (root.get("headers") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        body = null
                    )
                }
                RestMessage.Response::class.simpleName -> {
                    RestMessage.Response(
                        path = (root.get("path") as TextNode).asText(),
                        parameters = (root.get("parameters") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        headers = (root.get("headers") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        body = null,
                        statusCode = (root.get("statusCode") as IntNode).asInt()
                    )
                }
                else -> throw IllegalArgumentException("Unknown type: $type")
            }
        } else {
            val r = when(type) {
                RestMessage.Request::class.simpleName -> {
                    RestMessage.Request(
                        path = (root.get("path") as TextNode).asText(),
                        parameters = (root.get("parameters") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        headers = (root.get("headers") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        body = (body as ObjectNode).toString()
                    )
                }
                RestMessage.Response::class.simpleName -> {
                    RestMessage.Response(
                        path = (root.get("path") as TextNode).asText(),
                        parameters = (root.get("parameters") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        headers = (root.get("headers") as ObjectNode).fields().asSequence().map { it.key to it.value.asText() }.toMap(),
                        body = (body as ObjectNode).toString(),
                        statusCode = (root.get("statusCode") as IntNode).asInt()
                    )
                }
                else -> throw IllegalArgumentException("Unknown type: $type")
            }

            val deserializedBody  = SerializationConstants.getMessageDeserializer(r).readBody(r, bodyType!!.containedType(0))
            return when(r) {
                is RestMessage.Request -> RestMessage.Request(
                    path = r.path,
                    parameters = r.parameters,
                    headers = r.headers,
                    body = deserializedBody
                )
                is RestMessage.Response -> RestMessage.Response(
                    path = r.path,
                    parameters = r.parameters,
                    headers = r.headers,
                    body = deserializedBody,
                    statusCode = r.statusCode
                )
            }
        }
    }

    override fun createContextual(context: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val bodyType = property?.type ?: context.contextualType
        return RestMessageDeserializer().apply { this.bodyType = bodyType }
    }
}