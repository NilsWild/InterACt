package de.interact.utbi

import de.interact.domain.amqp.RoutingKey
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class TopicExchangeMatcher {

    fun match(routingKey: RoutingKey, topicBinding: RoutingKey): Boolean {
        if (routingKey == topicBinding) {
            return true
        }

        val replaced = topicBinding.key
            .replace("*", "([^\\.]+)")
            .replace("#", "([^\\.]+\\.?)+")

        val regexString = "^$replaced$"
        val regex = Pattern.compile(regexString)
        val matcher = regex.matcher(routingKey.key)

        return matcher.find()
    }
}