plugins {
    id("kmp-ktor")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation("site.addzero:addzero-tool-json:2025.09.29")
        }
    }
}
