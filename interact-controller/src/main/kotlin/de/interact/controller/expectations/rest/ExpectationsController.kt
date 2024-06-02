package de.interact.controller.expectations.rest

import de.interact.domain.expectations.execution.result.ExpectationsExecutionResult
import de.interact.domain.expectations.specification.api.ExpectationsCollectionsManagementService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/system-property-expectations")
class ExpectationsController(private var expectationManager: ExpectationsCollectionsManagementService) {

    @PostMapping
    fun saveExpectations(@RequestBody expectationsExecutionResult: ExpectationsExecutionResult) {
        expectationManager.handle(expectationsExecutionResult)
    }
}