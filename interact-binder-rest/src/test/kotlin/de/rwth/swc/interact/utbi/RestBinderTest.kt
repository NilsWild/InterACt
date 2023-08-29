package de.rwth.swc.interact.utbi

import de.rwth.swc.interact.controller.persistence.domain.INCOMING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.controller.persistence.domain.OUTGOING_INTERFACE_NODE_LABEL
import de.rwth.swc.interact.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.aggregator.AggregateWith
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.ArgumentsAggregator
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.util.*


internal class RestBinderTest {

    @Test
    fun `when protocol of given incomingInterface is REST canHandle returns true`() {
        val binder = RestBinder(mock(Neo4jClient::class.java))
        val componentInterface = mock(IncomingInterface::class.java)
        `when`(componentInterface.protocol).thenReturn(Protocol("REST"))
        assertThat(binder.canHandle(componentInterface)).isTrue()
    }

    @Test
    fun `when protocol of given outgoingInterface is REST canHandle returns true`() {
        val binder = RestBinder(mock(Neo4jClient::class.java))
        val componentInterface = mock(OutgoingInterface::class.java)
        `when`(componentInterface.protocol).thenReturn(Protocol("REST"))
        assertThat(binder.canHandle(componentInterface)).isTrue()
    }

    @DataNeo4jTest
    @EnableAutoConfiguration
    @ContextConfiguration(classes = [Neo4jConfig::class, RestBinder::class])
    @Nested
    internal inner class RestBinderNeo4jTest : Neo4jBaseTest() {

        @Autowired
        internal lateinit var restBinder: RestBinder

        @Autowired
        internal lateinit var neo4jClient: Neo4jClient

        @Transactional
        @ParameterizedTest
        @CsvSource(
            "REST, /test, true, GET",
            "REST, /test/{id}, false, POST",
            "REST, /test, true, PUT",
            "REST, /test, false, DELETE"
        )
        fun `given an outgoingInterface when an incoming interface is added and should be bound then the interfaces get bound`(
            @AggregateWith(OutgoingInterfaceAggregator::class) outgoingInterface: OutgoingInterface,
            @AggregateWith(IncomingInterfaceAggregator::class) incomingInterface: IncomingInterface
        ) {
            outgoingInterface.id = InterfaceId(UUID.fromString("c12ee40f-e945-4467-8826-d5b989b0c5ae"))
            storeComponentInterface(outgoingInterface)

            incomingInterface.id = InterfaceId(UUID.fromString("90791d36-3811-4c79-8138-ba0e4196ba0d"))
            storeComponentInterface(incomingInterface)
            restBinder.bindInterfaces(incomingInterface)
            val result = neo4jClient.query(
                "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL{id:\$outId})-[:BOUND_TO]->(i:$INCOMING_INTERFACE_NODE_LABEL{id:\$inId}) " +
                        "RETURN o, i"
            )
                .bind(outgoingInterface.id!!.toString()).to("outId")
                .bind(incomingInterface.id!!.toString()).to("inId")
                .fetch()
                .all()
            assertThat(result).withFailMessage("Expected 1 binding between interfaces, got ${result.size}").hasSize(1)
        }

        @Transactional
        @ParameterizedTest
        @CsvSource(
            "REST, /test, true, GET",
            "REST, /test/{id}, false, POST",
            "REST, /test, true, PUT",
            "REST, /test, false, DELETE"
        )
        fun `given an incomingInterface when an outgoing interface is added and should be bound then the interfaces get bound`(
            @AggregateWith(OutgoingInterfaceAggregator::class) outgoingInterface: OutgoingInterface,
            @AggregateWith(IncomingInterfaceAggregator::class) incomingInterface: IncomingInterface
        ) {
            incomingInterface.id = InterfaceId(UUID.fromString("90791d36-3811-4c79-8138-ba0e4196ba0d"))
            storeComponentInterface(incomingInterface)

            outgoingInterface.id = InterfaceId(UUID.fromString("c12ee40f-e945-4467-8826-d5b989b0c5ae"))
            storeComponentInterface(outgoingInterface)
            restBinder.bindInterfaces(outgoingInterface)
            val result = neo4jClient.query(
                "MATCH (o:$OUTGOING_INTERFACE_NODE_LABEL{id:\$outId})-[:BOUND_TO]->(i:$INCOMING_INTERFACE_NODE_LABEL{id:\$inId}) " +
                        "RETURN o, i"
            )
                .bind(outgoingInterface.id!!.toString()).to("outId")
                .bind(incomingInterface.id!!.toString()).to("inId")
                .fetch()
                .all()
            assertThat(result).withFailMessage("Expected 1 binding between interfaces, got ${result.size}").hasSize(1)
        }

        @Transactional
        internal fun storeComponentInterface(componentInterface: ComponentInterface) {
            val protocolDataString = componentInterface.protocolData.data.entries.joinToString(",") {
                "`protocolData.${it.key}`:\"${it.value}\""
            }
            when (componentInterface) {
                is IncomingInterface -> {
                    neo4jClient.query { "MERGE (:$INCOMING_INTERFACE_NODE_LABEL{id:\$id,protocol:\$protocol,$protocolDataString})" }
                        .bind(componentInterface.id!!.toString()).to("id")
                        .bind(componentInterface.protocol.protocol).to("protocol").run()
                }

                is OutgoingInterface -> {
                    neo4jClient.query { "MERGE (:$OUTGOING_INTERFACE_NODE_LABEL{id:\$id,protocol:\$protocol,$protocolDataString})" }
                        .bind(componentInterface.id!!.toString()).to("id")
                        .bind(componentInterface.protocol.protocol).to("protocol").run()
                }
            }
        }
    }
}

class OutgoingInterfaceAggregator : ArgumentsAggregator {
    override fun aggregateArguments(accesor: ArgumentsAccessor, context: ParameterContext): OutgoingInterface {
        return OutgoingInterface(
            Protocol(accesor.getString(0)),
            ProtocolData(
                mapOf(
                    Pair("url", accesor.getString(1)),
                    Pair("request", accesor.getString(2)),
                    Pair("method", accesor.getString(3))
                )
            )
        )
    }
}

class IncomingInterfaceAggregator : ArgumentsAggregator {
    override fun aggregateArguments(accesor: ArgumentsAccessor, context: ParameterContext): IncomingInterface {
        return IncomingInterface(
            Protocol(accesor.getString(0)),
            ProtocolData(
                mapOf(
                    Pair("url", accesor.getString(1)),
                    Pair("request", accesor.getString(2)),
                    Pair("method", accesor.getString(3))
                )
            )
        )
    }
}