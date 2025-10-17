//import  site.addzero.gradle.AdzeroJavaExtension

plugins {
    `kotlin-dsl`
    alias(libs.plugins.addzeroPublishBuddy)
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
    gradlePluginPortal()
}


dependencies {
    gradleApi()
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    compileOnly("org.yaml:snakeyaml:+")
    compileOnly("org.jetbrains.kotlin:kotlin-serialization:+")
    compileOnly("org.jetbrains.compose:compose-gradle-plugin:+")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:+")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:+")
    implementation("com.vanniktech:gradle-maven-publish-plugin:+")
    implementation("org.graalvm.buildtools.native:org.graalvm.buildtools.native.gradle.plugin:+")


    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:2.7.18")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:2.1.20")


}
