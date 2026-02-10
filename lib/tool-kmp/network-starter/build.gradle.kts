plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation(libs.addzero.tool.json)
        }
    }
}
