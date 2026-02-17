plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:method-semanticizer-spi:2026.02.15")
        }
    }
}
