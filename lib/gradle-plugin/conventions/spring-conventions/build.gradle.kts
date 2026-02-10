plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.org.springframework.boot.org.springframework.boot.gradle.plugin.v2)
    implementation(libs.io.spring.gradle.dependency.management.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.spring.org.jetbrains.kotlin.plugin.spring.gradle.plugin)
    gradleApi()
}
