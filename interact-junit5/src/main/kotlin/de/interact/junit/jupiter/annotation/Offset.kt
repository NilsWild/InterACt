package de.interact.junit.jupiter.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Offset(
    val value: Int
)
