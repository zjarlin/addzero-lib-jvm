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
    gradleApi()
}

gradlePlugin {
    plugins {
        create(project.name) {
            id = "site.addzero.apt-buddy"
            implementationClass = "site.addzero.gradle.plugin.aptbuddy.AptBuddyPlugin"
            displayName = "APT Buddy Plugin"
        }
    }
}
