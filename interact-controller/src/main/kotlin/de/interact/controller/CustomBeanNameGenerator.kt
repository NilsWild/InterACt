package de.interact.controller

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.AnnotationBeanNameGenerator

object CustomBeanNameGenerator : AnnotationBeanNameGenerator() {
    override fun generateBeanName(definition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        val beanClassName = definition.beanClassName
        return if (beanClassName!!.startsWith("de.interact.controller") &&
            (beanClassName.endsWith("Dao") || beanClassName.endsWith("Repository"))) {
            beanClassName.replace("de.interact.controller.", "")
                .replace("repository.", "")
                .split(".")
                .dropLast(1)
                .joinToString(".") + "." + super.generateBeanName(definition, registry)
        } else {
            super.generateBeanName(definition, registry)
        }
    }
}