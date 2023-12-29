package de.rwth.swc.interact.utbi

import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class TopicExchangeMatcher {

    fun match(routingKey: String, topicBinding: String): Boolean {
        if (routingKey == topicBinding) {
            return true
        }

        val replaced = topicBinding
            .replace("*", "([^\\.]+)")
            .replace("#", "([^\\.]+\\.?)+")

        val regexString = "^$replaced$"
        val regex = Pattern.compile(regexString)
        val matcher = regex.matcher(routingKey)

        return matcher.find()
    }
}