package de.rwth.swc.interact.junit.jupiter.annotation

import de.rwth.swc.interact.junit.jupiter.InterACtTestsExtension
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@TestTemplate
@ExtendWith(InterACtTestsExtension::class)
annotation class InterACtTest(
    val name: String = "{default_display_name}",
    val autoCloseArguments: Boolean = true
)

object InterACtTestConstants {
    const val DISPLAY_NAME_PLACEHOLDER: String = "{displayName}"
    const val ARGUMENTS_PLACEHOLDER: String = "{arguments}"
    const val ARGUMENTS_WITH_NAMES_PLACEHOLDER: String = "{argumentsWithNames}"
    const val DEFAULT_DISPLAY_NAME: String = "$ARGUMENTS_WITH_NAMES_PLACEHOLDER"
}