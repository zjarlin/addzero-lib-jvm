plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("ContextParameters")
    }
   val libs = versionCatalogs.named("libs")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("io-github-kyant0-backdrop").get())
            implementation(libs.findLibrary("io-github-fletchmckee-liquid-liquid").get())
            implementation(libs.findLibrary("io-github-kyant0-shapes").get())
//            implementation("org.jetbrains:annotations:26.0.2-1")
        }
    }
}
