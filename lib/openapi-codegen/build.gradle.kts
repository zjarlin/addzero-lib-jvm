plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("io-swagger-parser-v3-swagger-parser").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet").get())
            implementation(libs.findLibrary("com-squareup-kotlinpoet-ksp").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("io-kotest-kotest-property").get())
            implementation(libs.findLibrary("io-kotest-kotest-assertions-core").get())
            implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
        }
    }
}
