plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        jvmMain.dependencies {
//            implementation(libs.findLibrary("com-squareup-kotlinpoet").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
        }
    }
}
