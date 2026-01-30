plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.addzero.ksp.support.jdbc)
            implementation(libs.addzero.ksp.support)
            implementation(libs.tool.jdbc.model)

        }

    }
}
