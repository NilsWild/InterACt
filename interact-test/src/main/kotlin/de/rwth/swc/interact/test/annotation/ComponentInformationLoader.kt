package de.rwth.swc.interact.test.annotation

import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
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