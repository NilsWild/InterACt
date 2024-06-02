package de.interact.controller.observations

import de.interact.domain.testtwin.TestTwinManager
import de.interact.domain.testtwin.api.event.InteractionTestAddedEvent
import de.interact.domain.testtwin.api.event.UnitTestAddedEvent
import de.interact.domain.testtwin.spi.Components
import de.interact.domain.testtwin.spi.InterfaceAddedEventPublisher
import de.interact.domain.testtwin.spi.TestAddedEventPublisher
import de.interact.domain.testtwin.spi.Versions
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ObservationsConfig {

    @Bean
    fun interfaceAddedEventPublisher(applicationEventPublisher: ApplicationEventPublisher): InterfaceAddedEventPublisher {
        return InterfaceAddedEventPublisher {
            applicationEventPublisher.publishEvent(
                it
            )
        }
    }

    @Bean
    fun testAddedEventPublisher(applicationEventPublisher: ApplicationEventPublisher): TestAddedEventPublisher {
        return object : TestAddedEventPublisher {
            override fun publishNewUnitTest(newUnitTest: UnitTestAddedEvent) {
                applicationEventPublisher.publishEvent(
                    newUnitTest
                )
            }

            override fun publishNewInteractionTest(newInteractionTest: InteractionTestAddedEvent) {
                applicationEventPublisher.publishEvent(
                    newInteractionTest
                )
            }
        }
    }

    @Bean
    fun testTwinManager(
        components: Components,
        versions: Versions,
        interfaceAddedEventPublisher: InterfaceAddedEventPublisher,
        testAddedEventPublisher: TestAddedEventPublisher
    ): TestTwinManager {
        return TestTwinManager(
            components,
            versions,
            interfaceAddedEventPublisher,
            testAddedEventPublisher
        )
    }
}