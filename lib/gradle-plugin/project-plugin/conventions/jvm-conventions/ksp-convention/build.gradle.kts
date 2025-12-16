import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("site.addzero:gradle-tool-config-java:0.0.674")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.28")
    }
}
configureJ8("8")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}


dependencies {
//     implementation(libs.gradlePlugin.ksp)
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.28")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
//    implementation("site.addzero:java-convention:2025.12.19")
    implementation("site.addzero:kotlin-convention:2025.12.20")

//    implementation("site.addzero:kotlin-convention:2025.12.20")
    gradleApi()
}


