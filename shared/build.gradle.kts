
plugins {
//    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.androidLibrary)
    id("kmp-lib")
//    id("kmp-android-lib")
//    id("kmp-wasm")
//    id("kmp-core")
//    id("kmp-test")

}

kotlin {


    sourceSets {
        commonMain.dependencies {
        }
    }
}

//android {
//    namespace = "io.gitee.zjarlin.addzero.shared"
//    compileSdk = libs.versions.android.compileSdk.get().toInt()
//    compileOptions {
//        val toVersion = JavaVersion.toVersion(libs.versions.jdk.get())
//        sourceCompatibility = toVersion
//        targetCompatibility = toVersion
//
//    }
//    defaultConfig {
//        minSdk = libs.versions.android.minSdk.get().toInt()
//    }
//}
