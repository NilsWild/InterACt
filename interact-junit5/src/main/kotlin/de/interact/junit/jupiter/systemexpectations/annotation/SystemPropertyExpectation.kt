package de.interact.junit.jupiter.systemexpectations.annotation

import de.interact.junit.jupiter.systemexpectations.SystemPropertyTestExtension
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@TestTemplate
@ExtendWith(SystemPropertyTestExtension::class)
annotation class SystemPropertyExpectation(
    val name: String = "{default_display_name}",
)
