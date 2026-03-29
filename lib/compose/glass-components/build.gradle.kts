plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("org-jetbrains-compose-ui-ui-test").get())
        }
    }
}
