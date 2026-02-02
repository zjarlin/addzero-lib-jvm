package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import site.addzero.gradle.JavaConventionExtension

plugins {
    id("site.addzero.gradle.plugin.java-convention")
}

val javaConvention = the<JavaConventionExtension>()

afterEvaluate {
    dependencies {
        val lombok = "org.projectlombok:lombok:${javaConvention.lombokVersion.get()}"
        add("implementation", lombok)
        add("annotationProcessor", lombok)
    }
}
