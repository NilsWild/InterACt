package de.interact.controller.observations.graphql

import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.ClassNameTypeResolver
import org.springframework.graphql.execution.GraphQlSource.SchemaResourceBuilder


@Configuration
class GraphQlConfig {

    @Bean
    fun sourceBuilderCustomizer(): GraphQlSourceBuilderCustomizer {
        return GraphQlSourceBuilderCustomizer { builder: SchemaResourceBuilder ->
            val classNameTypeResolver = ClassNameTypeResolver()
            classNameTypeResolver.setClassNameExtractor { klass ->
                klass.simpleName.replace("Entity","")
            }
            builder.defaultTypeResolver(classNameTypeResolver)
        }
    }

}