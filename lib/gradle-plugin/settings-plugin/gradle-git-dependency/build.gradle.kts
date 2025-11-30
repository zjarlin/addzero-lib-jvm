import site.addzero.gradle.tool.configureJ8

version="2025.11.32"
buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("site.addzero:gradle-tool-config-java:0.0.674")
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
     implementation("me.champeau.includegit:me.champeau.includegit.gradle.plugin:0.3.2")
}

