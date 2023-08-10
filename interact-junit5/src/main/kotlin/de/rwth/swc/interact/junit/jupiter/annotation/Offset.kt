package de.rwth.swc.interact.junit.jupiter.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Offset(
    val value: Int
)
