package site.addzero.gradle.plugin

import site.addzero.gradle.JavaConventionExtension
import site.addzero.gradle.tool.configJunitPlatform
import site.addzero.gradle.tool.configUtf8
import site.addzero.gradle.tool.configureJ8
import site.addzero.gradle.tool.configureWithSourcesJar

plugins {
    `java-library`
}

// Create and register the extension
val javaConvention = extensions.create("javaConvention", JavaConventionExtension::class.java)

configureWithSourcesJar()
configUtf8()

// Configure after evaluation to allow users to set extension properties
afterEvaluate {
    configureJ8(javaConvention.jdkVersion.get())
}

configJunitPlatform()

// Configure dependencies after evaluation to use extension values
afterEvaluate {
    dependencies {
        add("testImplementation", "org.junit.jupiter:junit-jupiter-api:${javaConvention.junitVersion.get()}")
        add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:${javaConvention.junitVersion.get()}")
    }
}
