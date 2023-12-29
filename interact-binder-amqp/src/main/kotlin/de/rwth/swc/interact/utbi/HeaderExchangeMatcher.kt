package de.rwth.swc.interact.utbi

import org.springframework.stereotype.Component

@Component
class HeaderExchangeMatcher {

    fun match(headers: Map<String, String>, arguments: Map<String, String>): Boolean {
        val xMatchAll: Boolean =
            arguments.size == 1 || (arguments.containsKey("x-match") && arguments["x-match"] == "all")
        for (argument in arguments) {
            if (argument.key != "x-match") {
                if (headers.containsKey(argument.key) && headers[argument.key] == argument.value) {
                    if (!xMatchAll) return true
                } else {
                    if (xMatchAll) return false
                }
            }
        }
        return xMatchAll
    }
}