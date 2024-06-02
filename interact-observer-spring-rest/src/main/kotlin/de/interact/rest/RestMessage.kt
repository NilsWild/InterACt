package de.interact.rest

import com.fasterxml.jackson.annotation.JsonRawValue
import de.interact.utils.MultiMap

open class RestMessage<T>(
    val pathVariables: List<String>,
    val headers: MultiMap<String, String>,
    open val body: T?
)

class StringRestMessage(
    pathVariables: List<String>,
    headers: MultiMap<String, String>,
    @JsonRawValue
    override val body: String
) : RestMessage<String>(pathVariables, headers, body)