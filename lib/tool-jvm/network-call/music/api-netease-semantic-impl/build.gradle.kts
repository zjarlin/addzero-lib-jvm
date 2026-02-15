plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi"))
            implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi-helper"))
        }
    }
}
