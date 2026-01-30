plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.addzero.route.core)

            implementation(libs.addzero.ksp.support)
        }
    }
}
