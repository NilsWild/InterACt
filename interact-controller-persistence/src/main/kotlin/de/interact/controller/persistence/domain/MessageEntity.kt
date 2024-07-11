package de.interact.controller.persistence.domain

import de.interact.domain.shared.*
import org.springframework.data.annotation.Transient
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import java.util.*

const val MESSAGE_NODE_LABEL = "Message"
const val RECEIVED_MESSAGE_NODE_LABEL = "ReceivedMessage"
const val SENT_MESSAGE_NODE_LABEL = "SentMessage"
const val STIMULUS_NODE_LABEL = "Stimulus"
const val COMPONENT_RESPONSE_NODE_LABEL = "ComponentResponse"
const val ENVIRONMENT_RESPONSE_NODE_LABEL = "EnvironmentResponse"
const val SENT_BY_RELATIONSHIP_LABEL = "SENT_BY"
const val RECEIVED_BY_RELATIONSHIP_LABEL = "RECEIVED_BY"
const val DEPENDS_ON_RELATIONSHIP_LABEL = "DEPENDS_ON"
const val REACTION_TO_RELATIONSHIP_LABEL = "REACTION_TO"


@Node(MESSAGE_NODE_LABEL)
sealed class MessageEntity() : Entity(), Comparable<MessageEntity> {

    lateinit var payload: String
    var order: Int = 0

    @Relationship(type = TRIGGERED_MESSAGES_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var triggeredBy: ConcreteTestCaseEntity? = null

    @Transient
    var labels: Set<String> = setOf(MESSAGE_NODE_LABEL)

    override fun compareTo(other: MessageEntity): Int {
        return order - other.order
    }

}

@Node(SENT_MESSAGE_NODE_LABEL)
sealed class SentMessageEntity : MessageEntity() {

    @Relationship(type = SENT_BY_RELATIONSHIP_LABEL)
    lateinit var sentBy: OutgoingInterfaceEntity

    init {
        labels += setOf(SENT_MESSAGE_NODE_LABEL)
    }
}

@Node(RECEIVED_MESSAGE_NODE_LABEL)
sealed class ReceivedMessageEntity : MessageEntity() {

    @Relationship(type = RECEIVED_BY_RELATIONSHIP_LABEL)
    lateinit var receivedBy: IncomingInterfaceEntity

    @Relationship(DEPENDS_ON_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var dependencyFor: Set<ComponentResponseEntity> = setOf()

    init {
        labels += setOf(RECEIVED_MESSAGE_NODE_LABEL)
    }
}

@Node(STIMULUS_NODE_LABEL)
class StimulusEntity : ReceivedMessageEntity() {

    init {
        labels += setOf(STIMULUS_NODE_LABEL)
    }
}

@Node(COMPONENT_RESPONSE_NODE_LABEL)
class ComponentResponseEntity : SentMessageEntity() {


    init {
        labels += setOf(COMPONENT_RESPONSE_NODE_LABEL)
    }

    @Relationship(DEPENDS_ON_RELATIONSHIP_LABEL)
    lateinit var dependsOn: SortedSet<ReceivedMessageEntity>

    @Relationship(REACTION_TO_RELATIONSHIP_LABEL, direction = Relationship.Direction.INCOMING)
    var stimulusFor: Set<EnvironmentResponseEntity> = setOf()
}

@Node(ENVIRONMENT_RESPONSE_NODE_LABEL)
class EnvironmentResponseEntity : ReceivedMessageEntity() {

    init {
        labels += setOf(ENVIRONMENT_RESPONSE_NODE_LABEL)
    }

    @Relationship(REACTION_TO_RELATIONSHIP_LABEL)
    lateinit var reactionTo: ComponentResponseEntity
}

interface MessageReferenceProjection: EntityReferenceWithLabelsProjection {
}

fun MessageReferenceProjection.toEntityReference(): EntityReference<MessageId> {
    return when {
        labels.contains(STIMULUS_NODE_LABEL) -> EntityReference(StimulusMessageId(id), version)
        labels.contains(COMPONENT_RESPONSE_NODE_LABEL) -> EntityReference(ComponentResponseMessageId(id), version)
        labels.contains(ENVIRONMENT_RESPONSE_NODE_LABEL) -> EntityReference(EnvironmentResponseMessageId(id), version)
        else -> throw IllegalArgumentException("Unknown message type")
    }
}

interface SentMessageReferenceProjection: MessageReferenceProjection

fun SentMessageReferenceProjection.toEntityReference(): EntityReference<SentMessageId> {
    require(labels.contains(SENT_MESSAGE_NODE_LABEL))
    return when {
        labels.contains(COMPONENT_RESPONSE_NODE_LABEL) -> EntityReference(ComponentResponseMessageId(id),version)
        else -> throw IllegalArgumentException("Unknown message type")
    }
}

interface ComponentResponseReferenceProjection: SentMessageReferenceProjection

fun ComponentResponseReferenceProjection.toEntityReference(): EntityReference<ComponentResponseMessageId> {
    require(labels.contains(COMPONENT_RESPONSE_NODE_LABEL))
    return EntityReference(ComponentResponseMessageId(id), version)
}

interface ReceivedMessageReferenceProjection: MessageReferenceProjection

fun ReceivedMessageReferenceProjection.toEntityReference(): EntityReference<ReceivedMessageId> {
    require(labels.contains(RECEIVED_MESSAGE_NODE_LABEL))
    return when {
        labels.contains(STIMULUS_NODE_LABEL) -> EntityReference(StimulusMessageId(id),version)
        labels.contains(ENVIRONMENT_RESPONSE_NODE_LABEL) -> EntityReference(EnvironmentResponseMessageId(id),version)
        else -> throw IllegalArgumentException("Unknown message type")
    }
}

interface EnvironmentResponseReferenceProjection: ReceivedMessageReferenceProjection

fun EnvironmentResponseReferenceProjection.toEntityReference(): EntityReference<EnvironmentResponseMessageId> {
    require(labels.contains(ENVIRONMENT_RESPONSE_NODE_LABEL))
    return EntityReference(EnvironmentResponseMessageId(id),version)
}

interface StimulusReferenceProjection: ReceivedMessageReferenceProjection

fun StimulusReferenceProjection.toEntityReference(): EntityReference<StimulusMessageId> {
    require(labels.contains(STIMULUS_NODE_LABEL))
    return EntityReference(StimulusMessageId(id),version)
}

fun EntityReference<MessageId>.toEntity(): MessageEntity {
    return when (id) {
        is StimulusMessageId -> stimulusEntityReference(id as StimulusMessageId, version)
        is ComponentResponseMessageId -> componentResponseEntityReference(id as ComponentResponseMessageId, version)
        is EnvironmentResponseMessageId -> environmentResponseEntityReference(id as EnvironmentResponseMessageId, version)
    }
}

fun EntityReference<SentMessageId>.toEntity(): SentMessageEntity {
    return when (id) {
        is ComponentResponseMessageId -> componentResponseEntityReference(id as ComponentResponseMessageId, version)
        else -> throw IllegalArgumentException("Unknown message type")
    }
}

fun EntityReference<ReceivedMessageId>.toEntity(): ReceivedMessageEntity {
    return when (id) {
        is StimulusMessageId -> stimulusEntityReference(id as StimulusMessageId, version)
        is EnvironmentResponseMessageId -> environmentResponseEntityReference(id as EnvironmentResponseMessageId, version)
        else -> throw IllegalArgumentException("Unknown message type")
    }
}

fun EntityReference<ComponentResponseMessageId>.toEntity(): ComponentResponseEntity {
    return componentResponseEntityReference(id,version)
}

fun EntityReference<EnvironmentResponseMessageId>.toEntity(): EnvironmentResponseEntity {
    return environmentResponseEntityReference(id,version)
}

fun EntityReference<StimulusMessageId>.toEntity(): StimulusEntity {
    return stimulusEntityReference(id,version)
}

fun stimulusEntityReference(id: StimulusMessageId, version: Long?): StimulusEntity {
    return StimulusEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun stimulusEntity(id: StimulusMessageId, version: Long?, value: String, order: Int, receivedBy: IncomingInterfaceEntity): StimulusEntity {
    return stimulusEntityReference(id,version).also {
        it.payload = value
        it.order = order
        it.receivedBy = receivedBy
    }
}

fun componentResponseEntityReference(id: ComponentResponseMessageId, version: Long?): ComponentResponseEntity {
    return ComponentResponseEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun componentResponseEntity(id: ComponentResponseMessageId, version: Long?, value: String, order: Int, sentBy: OutgoingInterfaceEntity): ComponentResponseEntity {
    return componentResponseEntityReference(id,version).also {
        it.payload = value
        it.order = order
        it.sentBy = sentBy
    }
}

fun environmentResponseEntityReference(id: EnvironmentResponseMessageId, version: Long?): EnvironmentResponseEntity {
    return EnvironmentResponseEntity().also {
        it.id = id.value
        it.version = version
    }
}

fun environmentResponseEntity(id: EnvironmentResponseMessageId, version: Long?, value: String, order: Int, receivedBy: IncomingInterfaceEntity): EnvironmentResponseEntity {
    return environmentResponseEntityReference(id,version).also {
        it.payload = value
        it.order = order
        it.receivedBy = receivedBy
    }
}