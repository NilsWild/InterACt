package de.interact.controller.observations.graphql

import de.interact.controller.observations.repository.VersionRepository
import de.interact.controller.persistence.domain.VersionEntity
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class VersionController(
    private val versionsRepository: VersionRepository
) {

    @QueryMapping
    fun versions(@Argument componentIdentifier: String): List<VersionEntity> {
        return versionsRepository.findByVersionOfIdentifier(componentIdentifier)
    }

    @QueryMapping
    fun version(@Argument componentIdentifier: String, @Argument versionIdentifier: String): VersionEntity? {
        return versionsRepository.findByVersionOfIdentifierAndIdentifier(componentIdentifier, versionIdentifier)
    }

}