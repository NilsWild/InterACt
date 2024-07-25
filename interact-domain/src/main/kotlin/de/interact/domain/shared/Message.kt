package de.interact.domain.shared

interface Message<BODY> {
    val body: BODY?
}