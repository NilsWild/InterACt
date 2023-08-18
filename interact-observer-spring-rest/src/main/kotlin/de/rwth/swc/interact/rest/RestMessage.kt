package de.rwth.swc.interact.rest

import com.fasterxml.jackson.annotation.JsonRawValue

open class RestMessage<T>(
    val pathVariables: MutableMap<String, String>,
    val headers: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>,
    open val body: T
) {
}

class StringRestMessage(
    pathVariables: MutableMap<String, String>,
    headers: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>,
    @JsonRawValue
    override val body: String
) : RestMessage<String>(pathVariables, headers, body) {
}