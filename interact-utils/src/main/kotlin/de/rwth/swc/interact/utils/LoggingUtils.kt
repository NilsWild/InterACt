package de.rwth.swc.interact.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import kotlin.reflect.full.companionObject

/**
 * The logger is created by the logger() extension function.
 * Marker interface to keep the Any type clean.
 */
interface Logging

/**
 * Get the class for logging purposes.
 * If the class is a companion object, the enclosing class is returned.
 * That way we can use the logger either as a static field in the companion object or as a field in the class.
 */
inline fun <T : Any> getClassForLogging(javaClass: Class<T>): Class<*> {
    return javaClass.enclosingClass?.takeIf {
        it.kotlin.companionObject?.java == javaClass
    } ?: javaClass
}

/**
 * Extension function for the Logging interface.
 * This function returns a logger for the class that implements the Logging interface.
 * We use the reified type parameter to get the class of the implementing class without reflection.
 * Note that this has the side effect that the logger name of the class that declares it is used.
 */
inline fun <reified T : Logging> T.logger(): Logger = getLogger(getClassForLogging(T::class.java))