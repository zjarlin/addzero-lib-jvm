plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.site.addzero.ksp.support.jdbc)
            implementation(libs.site.addzero.ksp.support)
            implementation(libs.site.addzero.tool.jdbc.model)

        }

    }
}
