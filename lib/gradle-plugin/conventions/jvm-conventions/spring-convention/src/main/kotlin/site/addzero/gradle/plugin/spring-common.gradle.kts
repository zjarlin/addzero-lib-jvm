package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import site.addzero.gradle.SpringConventionExtension

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val springConvention = extensions.create("springConvention", SpringConventionExtension::class.java)

afterEvaluate {
    dependencies {
        val bootVersion = springConvention.springBootVersion.get()
        implementation(platform("org.springframework.boot:spring-boot-dependencies:$bootVersion"))

        testImplementation("org.springframework.boot:spring-boot-starter-test:$bootVersion")
        testImplementation("org.springframework.boot:spring-boot-starter-web:$bootVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${springConvention.junitVersion.get()}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${springConvention.junitVersion.get()}")
        testImplementation("com.h2database:h2:${springConvention.h2Version.get()}")
    }
}
