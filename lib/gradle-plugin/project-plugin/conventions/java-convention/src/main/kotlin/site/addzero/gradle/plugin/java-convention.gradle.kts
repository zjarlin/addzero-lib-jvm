package site.addzero.gradle.plugin

import site.addzero.gradle.tool.configJunitPlatform
import site.addzero.gradle.tool.configUtf8
import site.addzero.gradle.tool.configureJ8
import site.addzero.gradle.tool.configureWithSourcesJar

plugins {
    `java-library`
}

configureWithSourcesJar()
configUtf8()

// Get JDK version from project property or use default
val javaVersion = project.findProperty("javaConvention.jdkVersion")?.toString() ?: "8"
configureJ8(javaVersion)

configJunitPlatform()

// Get dependency versions from project properties or use defaults
val junitVersion = project.findProperty("javaConvention.junitVersion")?.toString() ?: "5.8.1"
val lombokVersion = project.findProperty("javaConvention.lombokVersion")?.toString() ?: "1.18.24"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    implementation("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}
