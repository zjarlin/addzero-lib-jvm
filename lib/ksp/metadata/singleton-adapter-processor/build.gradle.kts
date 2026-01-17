plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:metadata:singleton-adapter-api"))
        }
        jvmMain.dependencies {
            implementation(libs.kotlinpoet.ksp)
        }
    }
}
