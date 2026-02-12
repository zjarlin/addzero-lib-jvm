plugins {
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:network-starter:2025.09.30")
        }
    }
}
