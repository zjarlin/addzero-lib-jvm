package site.addzero.gradle.plugin

import site.addzero.gradle.*

plugins {
    id("site.addzero.gradle.plugin.java-convention")
}

val javaConvention = the<JavaConventionExtension>()

afterEvaluate {
    dependencies {
        implementation("org.projectlombok:lombok:${javaConvention.lombokVersion.get()}")
        annotationProcessor("org.projectlombok:lombok:${javaConvention.lombokVersion.get()}")
    }
}
