package site.addzero.gradle.plugin

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val componentModules = rootProject.subprojects
    .filter {
        it.name.startsWith("addzero-compose-native-component-") &&
            it.name != "addzero-compose-native-component"
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
