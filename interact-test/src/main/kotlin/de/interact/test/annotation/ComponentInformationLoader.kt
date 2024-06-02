package de.interact.test.annotation

import de.interact.test.ComponentInformationLoader
import de.interact.test.PropertiesBasedComponentInformationLoader
import kotlin.reflect.KClass

/**
 * This Annotation can be used to specify the ComponentInformationLoader to use for the tests contained
 * in the annotated class.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ComponentInformationLoader(
    val value: KClass<out ComponentInformationLoader> = PropertiesBasedComponentInformationLoader::class
)