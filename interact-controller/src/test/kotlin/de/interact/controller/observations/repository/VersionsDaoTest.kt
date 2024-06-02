package de.interact.controller.observations.repository

import de.interact.controller.Neo4jBaseTest
import de.interact.domain.shared.*
import de.interact.domain.testtwin.Component
import de.interact.domain.testtwin.Version
import de.interact.domain.testtwin.VersionIdentifier
import de.interact.domain.testtwin.abstracttest.AbstractTestCase
import de.interact.domain.testtwin.abstracttest.AbstractTestCaseIdentifier
import de.interact.domain.testtwin.abstracttest.concretetest.ConcreteTestCaseIdentifier
import de.interact.domain.testtwin.abstracttest.concretetest.TestParameter
import de.interact.domain.testtwin.abstracttest.concretetest.UnitTest
import de.interact.domain.testtwin.abstracttest.concretetest.message.ComponentResponseMessage
import de.interact.domain.testtwin.abstracttest.concretetest.message.EnvironmentResponseMessage
import de.interact.domain.testtwin.abstracttest.concretetest.message.Message
import de.interact.domain.testtwin.abstracttest.concretetest.message.StimulusMessage
import io.kotest.matchers.collections.shouldContainAllIgnoringFields
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import org.instancio.Instancio
import org.instancio.Select
import org.instancio.junit.InstancioExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest

@DataNeo4jTest
@ExtendWith(InstancioExtension::class)
class VersionsDaoTest : Neo4jBaseTest() {

    @Autowired
    lateinit var dao: VersionsDao

    @Autowired
    lateinit var componentDao: ComponentDao

    @Test
    fun `save and retrieve component and version`() {
        var component = Instancio.create(Component::class.java)
        component = componentDao.add(component)
        val versionIdentifier = Instancio.create(VersionIdentifier::class.java)
        val stimulus = Instancio.of(StimulusMessage::class.java).set(
            Select.field("order"),
            0
        ).create()
        val componentResponse = Instancio.of(ComponentResponseMessage::class.java).set(
            Select.field("order"),
            1
        ).set(
            Select.field("dependsOn"),
            setOf(stimulus)
        ).create()
        val environmentResponse = Instancio.of(EnvironmentResponseMessage::class.java).set(
            Select.field("order"),
            2
        ).set(
            Select.field("reactionTo"),
            componentResponse
        ).create()
        val testCases = setOf(
            UnitTest(
                Instancio.create(UnitTestId::class.java),
                Instancio.create(ConcreteTestCaseIdentifier::class.java),
                listOf(
                    TestParameter(
                        stimulus.value.value
                    ), TestParameter(
                        environmentResponse.value.value
                    )
                ),
                sortedSetOf(
                    stimulus,
                    componentResponse,
                    environmentResponse
                ),
                TestState.TestFinishedState.Succeeded
            ),
            UnitTest(
                Instancio.create(UnitTestId::class.java),
                Instancio.create(ConcreteTestCaseIdentifier::class.java),
                listOf(
                    TestParameter(
                        stimulus.value.value
                    ), TestParameter(
                        environmentResponse.value.value
                    )
                ),
                sortedSetOf(
                    stimulus,
                    componentResponse,
                    environmentResponse
                ),
                TestState.TestFinishedState.Succeeded
            )
        )
        val version = Version(
            EntityReference(component.id, component.version),
            versionIdentifier,
            setOf(
                AbstractTestCase(
                    Instancio.create(AbstractTestId::class.java),
                    Instancio.create(AbstractTestCaseIdentifier::class.java),
                    testCases
                )
            ),
            testCases.asSequence().flatMap { it.triggeredMessages }.filterIsInstance<Message.ReceivedMessage>()
                .map { it.receivedBy }.toSet(),
            testCases.asSequence().flatMap { it.triggeredMessages }.filterIsInstance<Message.SentMessage>()
                .map { it.sentBy }.toSet(),
            Instancio.create(VersionId::class.java)
        )

        dao.save(version)
        val retrievedVersion = dao.findVersionByComponentAndId(component.id, version.id)!!

        //TODO retrievedVersion.shouldBeEqualToIgnoringFields(version, Entity<*>::version)
    }
}