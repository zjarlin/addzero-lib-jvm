package site.addzero.gradle.plugin

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

// Configure Java for IntelliJ platform
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
