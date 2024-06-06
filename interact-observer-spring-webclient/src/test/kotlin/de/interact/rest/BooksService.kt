package de.interact.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange


interface BooksService {

    @GetExchange("/books")
    fun getBooks(): List<Book?>?

    @GetExchange("/books/{id}")
    fun getBook(@PathVariable id: Long): Book?

    @PostExchange("/books")
    fun saveBook(@RequestBody book: Book?): Book?

    @DeleteExchange("/books/{*id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Void?>?
}