plugins {
    id("kmp-ktor")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation(libs.addzero.tool.json)
        }
    }
}
