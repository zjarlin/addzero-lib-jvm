plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-spi"))
        }
    }
}
