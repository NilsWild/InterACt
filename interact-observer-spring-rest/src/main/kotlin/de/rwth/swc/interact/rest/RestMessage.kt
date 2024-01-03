package de.rwth.swc.interact.rest

import com.fasterxml.jackson.annotation.JsonRawValue
import org.springframework.util.MultiValueMap

open class RestMessage<T>(
    val pathVariables: List<String>,
    val headers: MultiValueMap<String, String>,
    open val body: T?
)

class StringRestMessage(
    pathVariables: List<String>,
    headers: MultiValueMap<String, String>,
    @JsonRawValue
    override val body: String
) : RestMessage<String>(pathVariables, headers, body)