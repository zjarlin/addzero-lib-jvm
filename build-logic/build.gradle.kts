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
    compileOnly(libs.gradlePlugin.jetbrainsCompose)
    compileOnly(libs.gradlePlugin.kotlinSerialization)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(libs.org.graalvm.buildtools.native.gradle.plugin)
    
    implementation(libs.gradlePlugin.dependencyManagement)
    implementation(libs.gradlePlugin.springBoot)
    implementation(libs.gradlePlugin.kotlinSpring)
}
