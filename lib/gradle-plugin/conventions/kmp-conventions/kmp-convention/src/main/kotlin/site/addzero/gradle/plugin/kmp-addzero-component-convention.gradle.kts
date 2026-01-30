package site.addzero.gradle.plugin

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val componentModules = rootProject.subprojects
    .filter {
        it.name.startsWith("compose-native-component-") &&
            it.name != "compose-native-component"
    }

kotlin {
    sourceSets {
        commonMain.dependencies {
            componentModules.forEach {
                implementation(it)
            }
        }
    }
}
