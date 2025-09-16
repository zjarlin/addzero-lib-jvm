plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
//    kotlin("plugin.serialization") version "1.9.10"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

gradlePlugin {
    plugins {
        create(project.name) {

            id = "ksp-buddy"
            implementationClass = "site.addzero.gradle.plugin.kspbuddy.KspBuddyPlugin"
            displayName = "KSP Buddy Plugin"
        }
    }
}
