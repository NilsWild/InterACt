package de.interact.controller.expectations.validation

import de.interact.domain.expectations.validation.api.ValidationPlansManager
import de.interact.domain.expectations.validation.spi.Interfaces
import de.interact.domain.expectations.validation.spi.Tests
import de.interact.domain.expectations.validation.spi.UnitTestBasedInteractionExpectations
import de.interact.domain.expectations.validation.spi.ValidationPlans
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExpectationsValidationConfig {

    @Bean
    fun validationPlansManager(
        tests: Tests,
        validationPlans: ValidationPlans,
        unitTestBasedInteractionExpectations: UnitTestBasedInteractionExpectations,
        interfaces: Interfaces
    ): ValidationPlansManager {
        return ValidationPlansManager(
            tests,
            validationPlans,
            unitTestBasedInteractionExpectations,
            interfaces
        )
    }
}