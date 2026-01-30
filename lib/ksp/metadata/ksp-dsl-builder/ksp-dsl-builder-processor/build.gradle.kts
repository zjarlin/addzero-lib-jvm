plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
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
