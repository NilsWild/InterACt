package de.interact.rest

import com.fasterxml.jackson.annotation.JsonRawValue
import de.interact.utils.MultiMap

open class RestMessage<T>(
    val pathVariables: List<String>,
    val parameters: Map<String, String>,
    val headers: Map<String, String>,
    open val body: T?
)

class StringRestMessage(
    pathVariables: List<String>,
    parameters: Map<String,String>,
    headers: Map<String, String>,
    @JsonRawValue
    override val body: String
) : RestMessage<String>(pathVariables, parameters, headers, body)