package de.interact.domain.expectations.derivation.api

import de.interact.domain.expectations.derivation.interaction.Interaction
import de.interact.domain.expectations.derivation.interactionexpectation.InteractionExpectation
import de.interact.domain.expectations.derivation.spi.EventPublisher
import de.interact.domain.expectations.derivation.spi.Interactions
import de.interact.domain.expectations.derivation.spi.SystemInteractionExpectations
import de.interact.domain.expectations.derivation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.instancio.Instancio
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class InteractionExpectationDerivationServiceTest {

    @MockK
    private lateinit var interactions: Interactions

    @MockK
    private lateinit var unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations

    @MockK
    private lateinit var systemInteractionExpectations: SystemInteractionExpectations

    @MockK(relaxed = true)
    private lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    private lateinit var service: InteractionExpectationDerivationService

    @Test
    fun `handleNewUnitTestCase should save new unit test based interaction expectations for that test`() {
        val event = Instancio.create(UnitTestAddedEvent::class.java)
        every {
            unitTestBasedInteractionExpectations.save(any())
        } returnsArgument 0
        every {
            interactions.findForTest(event.test.id)
        } returns setOf(Instancio.create(Interaction::class.java))

        service.onUnitTestCaseAdded(event)
        verify {
            unitTestBasedInteractionExpectations.save(
                match {
                    it.derivedFrom == event.test
                }
            )
        }
    }

}