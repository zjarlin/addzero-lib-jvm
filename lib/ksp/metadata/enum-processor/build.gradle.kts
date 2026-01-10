plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        jvmMain.dependencies {
//            implementation(libs.kotlinpoet)
            implementation(libs.kotlinpoet.ksp)
        }
    }
}
