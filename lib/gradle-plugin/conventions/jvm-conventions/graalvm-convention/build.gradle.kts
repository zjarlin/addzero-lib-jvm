plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.org.graalvm.buildtools.native.org.graalvm.buildtools.native.gradle.plugin)
    gradleApi()
}
description = "GraalVM convention for JVM projects"
