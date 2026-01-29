plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.org.graalvm.buildtools.native.gradle.plugin)
    gradleApi()
}
