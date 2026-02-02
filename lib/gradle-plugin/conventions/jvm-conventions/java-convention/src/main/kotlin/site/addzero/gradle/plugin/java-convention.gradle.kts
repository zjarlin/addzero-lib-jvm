package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.JavaConventionExtension
import site.addzero.gradle.tool.*

plugins {
  `java-library`
}

val javaConvention = extensions.create("javaConvention", JavaConventionExtension::class.java)

configureWithSourcesJar()
configUtf8()
configJunitPlatform()

afterEvaluate {
  configureJ8(javaConvention.jdkVersion.get())
}

afterEvaluate {
  dependencies {
    val junitVersion = javaConvention.junitVersion.get()
    add("testImplementation", "org.junit.jupiter:junit-jupiter-api:$junitVersion")
    add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
  }
}
