plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-dsl-builder-core:2025.09.29")
        }
        jvmMain.dependencies {
            implementation(libs.hutool.all)
        }
    }
}
