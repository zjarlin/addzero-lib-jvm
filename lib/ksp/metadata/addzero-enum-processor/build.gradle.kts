plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        jvmMain.dependencies {
//            implementation("com.squareup:kotlinpoet:1.14.2")
            implementation(libs.kotlinpoet.ksp)
        }
    }
}
