package site.addzero.spring

import site.addzero.gradle.AdzeroSpringBuddyExtension




val create = extensions.create<AdzeroSpringBuddyExtension>("addzeroSpringBuddy").apply {
    version="2.7.5"
}


plugins {
    id("site.addzero.jvm.kotlin-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${create.version}"))
}


