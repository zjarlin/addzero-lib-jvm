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
// IntelliJ Platform plugins require Java 11+
configureJ8("17")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("site.addzero:kotlin-convention:2025.12.20")
    /////////////////intellij///////////////
    implementation("org.jetbrains.intellij.plugins:gradle-intellij-plugin:1.17.2")
    implementation(libs.org.jetbrains.changelog.gradle.plugin)
      gradleApi()
}

gradlePlugin {
    plugins {
        create("intellijBase") {
            id = "site.addzero.gradle.plugin.intellij-base"
            implementationClass = "site.addzero.gradle.plugin.IntellijBasePlugin"
        }
        create("intellijCore") {
            id = "site.addzero.gradle.plugin.intellij-core"
            implementationClass = "site.addzero.gradle.plugin.IntellijCorePlugin"
        }
        create("intellijPlatform") {
            id = "site.addzero.gradle.plugin.intellij-platform"
            implementationClass = "site.addzero.gradle.plugin.IntellijPlatformPlugin"
        }
        create("intellijInfo") {
            id = "site.addzero.gradle.plugin.intellij-info"
            implementationClass = "site.addzero.gradle.plugin.IntellijInfoPlugin"
        }
    }
}


