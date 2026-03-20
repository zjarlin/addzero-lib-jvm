plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.de.jensklingenberg.ktorfit.ktorfit.lib)
            implementation(libs.site.addzero.tool.json)
        }
    }
}
