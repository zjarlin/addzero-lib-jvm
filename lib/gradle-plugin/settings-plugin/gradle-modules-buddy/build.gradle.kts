import site.addzero.gradle.tool.configureJdk
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.site.addzero.gradle.tool.config.java)
    }
}
configureJdk("8")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

val catalogLibs = versionCatalogs.named("libs")

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
    testRuntimeOnly(catalogLibs.findLibrary("org-junit-platform-junit-platform-launcher").get())
}

tasks.test {
    useJUnitPlatform()
}
