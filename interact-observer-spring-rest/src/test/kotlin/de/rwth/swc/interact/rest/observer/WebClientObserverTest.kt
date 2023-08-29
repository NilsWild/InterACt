package de.rwth.swc.interact.rest.observer

import de.rwth.swc.interact.domain.AbstractTestCaseName
import de.rwth.swc.interact.domain.ConcreteTestCaseName
import de.rwth.swc.interact.domain.TestMode
import de.rwth.swc.interact.observer.TestObserver
import de.rwth.swc.interact.rest.Book
import de.rwth.swc.interact.rest.BooksService
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.matchers.Times.exactly
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

internal class WebClientObserverTest {
    @Test
    fun test() {
        TestObserver.startObservation(
            this.javaClass,
            AbstractTestCaseName("test"),
            ConcreteTestCaseName("test"),
            listOf(),
            TestMode.UNIT
        )
        val server = startClientAndServer(9000)
        MockServerClient("localhost", 9000)
            .`when`(
                request()
                    .withPath("/books")
                    .withMethod(HttpMethod.POST.name()),
                exactly(1)
            )
            .respond(
                response()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withBody("{\"id\":1,\"title\":\"Book_1\",\"author\":\"Author_1\",\"year\":1998}")
            )

        val book1 = Book(1, "Book_1", "Author_1", 1998)
        val webClient = WebClient.builder()
            .baseUrl("http://localhost:9000")
            .filter(WebClientObserver(false))
            .build();
        val httpServiceProxyFactory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build()
        val booksService = httpServiceProxyFactory.createClient(BooksService::class.java)
        val book = booksService.saveBook(book1)
        server.stop()
    }
}