package de.interact.controller.expectations.derivation.repository

import de.interact.controller.persistence.domain.*
import de.interact.domain.expectations.derivation.interaction.Interaction
import de.interact.domain.expectations.derivation.interaction.Reaction
import de.interact.domain.expectations.derivation.interaction.Stimulus
import de.interact.domain.expectations.derivation.spi.Interactions
import de.interact.domain.shared.*
import org.springframework.stereotype.Service
import java.util.*

interface TestInteractionsRepository {
    fun findTestById(id: UUID): TestCaseWithInteractions?
}

@Service
class InteractionsDao(private val testInteractionsRepository: TestInteractionsRepository): Interactions{
    @Suppress("UNCHECKED_CAST")
    override fun findForTest(testId: TestId): Set<Interaction> {
        return testInteractionsRepository.findTestById(testId.value)?.let { test ->
            test.triggeredMessages
                .filter { it.labels.contains(COMPONENT_RESPONSE_NODE_LABEL) }
                .mapNotNull { interactionStimulus ->
                    val interaction = Interaction(
                        Stimulus(
                            interactionStimulus.toEntityReference() as EntityReference<ComponentResponseMessageId>,
                            interactionStimulus.sentBy!!.toEntityReference() as EntityReference<OutgoingInterfaceId>
                        ),
                        test.triggeredMessages
                            .filter { it.labels.contains(ENVIRONMENT_RESPONSE_NODE_LABEL) }
                            .filter { it.reactionTo!!.id == interactionStimulus.id }
                            .map { reactionMessage ->
                                Reaction(
                                    reactionMessage.toEntityReference() as EntityReference<EnvironmentResponseMessageId>,
                                    reactionMessage.receivedBy!!.toEntityReference() as EntityReference<IncomingInterfaceId>
                                )
                            }.toSet()
                    )
                    if (interaction.reactions.isEmpty()) null else interaction
                }.toSet()
        } ?: emptySet()
    }
}

interface TestCaseWithInteractions {
    val triggeredMessages: Set<TriggeredMessage>

    interface TriggeredMessage: MessageReferenceProjection {
        //needed to correctly fetch sortedset even though we do not care about order
        val order: Int
        val reactionTo: MessageReferenceProjection?
        val sentBy: InterfaceReferenceProjection?
        val receivedBy: InterfaceReferenceProjection?
    }
}