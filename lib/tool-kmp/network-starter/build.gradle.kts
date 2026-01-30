plugins {
    id("site.addzero.gradle.plugin.kmp-ktor-convention")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation(libs.addzero.tool.json)
        }
    }
}
