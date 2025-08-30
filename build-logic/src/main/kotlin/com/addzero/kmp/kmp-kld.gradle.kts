plugins {
    id("org.jetbrains.kotlin.multiplatform")
//    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:kld:addzero-kaleidoscope-ksp"))
        }
    }
}
