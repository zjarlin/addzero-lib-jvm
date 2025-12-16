import site.addzero.gradle.tool.configureJ8

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
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gradle.tool.config.java)
    implementation(libs.gradlePlugin.kotlin)

    gradleApi()
}

gradlePlugin {
    plugins {
        create("spring-common-convention") {
            id = "site.addzero.gradle.plugin.spring-common-convention"
            implementationClass = "site.addzero.gradle.plugin.SpringCommonConventionPlugin"
        }
        create("spring-starter-convention") {
            id = "site.addzero.gradle.plugin.spring-starter-convention"
            implementationClass = "site.addzero.gradle.plugin.SpringStarterConventionPlugin"
        }
        create("spring-app-convention") {
            id = "site.addzero.gradle.plugin.spring-app-convention"
            implementationClass = "site.addzero.gradle.plugin.SpringAppConventionPlugin"
        }
    }
}