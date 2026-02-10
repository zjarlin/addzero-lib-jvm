import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.site.addzero.gradle.tool.config.java)
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
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.site.addzero.gradle.tool.config.java)
    implementation(libs.site.addzero.gradle.plugin.java.convention.site.addzero.gradle.plugin.java.convention.gradle.plugin)
    gradleApi()
}
