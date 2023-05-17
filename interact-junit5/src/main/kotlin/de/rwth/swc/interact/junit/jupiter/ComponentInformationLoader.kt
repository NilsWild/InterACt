package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.test.ComponentInformationLoader
import de.rwth.swc.interact.test.PropertiesBasedComponentInformationLoader
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ComponentInformationLoader(
    val value: KClass<out ComponentInformationLoader> = PropertiesBasedComponentInformationLoader::class
)