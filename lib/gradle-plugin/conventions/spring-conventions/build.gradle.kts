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
    implementation(libs.boot.org.springframework.boot.gradle.plugin)
    implementation(libs.dependency.management.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.spring.gradle.plugin)
    gradleApi()
}
