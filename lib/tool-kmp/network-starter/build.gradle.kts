plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ktor")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktorfit.lib)
            implementation(libs.addzero.tool.json)
        }
    }
}
