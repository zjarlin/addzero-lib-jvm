plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:method-semanticizer-spi:2026.02.15")
            implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-helper"))
        }
    }
}
