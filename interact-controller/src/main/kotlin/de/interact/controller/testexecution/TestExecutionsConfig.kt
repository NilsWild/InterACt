package de.interact.controller.testexecution

import de.interact.domain.testexecution.IntegrationManager
import de.interact.domain.testexecution.api.Integrations
import de.interact.domain.testexecution.spi.TestExecutionRequests
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestExecutionsConfig {

    @Bean
    fun integrations(testExecutionRequests: TestExecutionRequests): Integrations {
        return IntegrationManager(testExecutionRequests)
    }
}