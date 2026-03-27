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
     implementation(catalogLibs.findLibrary("me-champeau-includegit-me-champeau-includegit-gradle-plugin").get())
}
