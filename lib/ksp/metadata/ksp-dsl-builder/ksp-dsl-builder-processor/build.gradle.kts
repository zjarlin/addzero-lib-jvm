plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.dsl.builder.core)
        }
        jvmMain.dependencies {
            implementation(libs.hutool.all)
        }
    }
}
