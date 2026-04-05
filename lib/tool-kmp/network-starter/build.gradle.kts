plugins {
  id("site.addzero.buildlogic.kmp.kmp-ktor-client")
  id("site.addzero.buildlogic.kmp.kmp-koin-core")
}
val libs = versionCatalogs.named("libs")

configurations.matching {
  it.name == "kotlinCompilerPluginClasspathWasmJsMain" ||
    it.name == "kotlinCompilerPluginClasspathWasmJsTest"
}.configureEach {
  exclude(group = "io.insert-koin", module = "koin-compiler-plugin")
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      // 跨平台存储（自动对应 Android SharedPreferences / iOS NSUserDefaults）
      implementation("com.russhwolf:multiplatform-settings:1.1.1")
      // 协程支持（suspend 方法）
//      implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.1")


      implementation(libs.findLibrary("de-jensklingenberg-ktorfit-ktorfit-lib").get())
      implementation(project(":lib:tool-kmp:tool-coll"))
      implementation(libs.findLibrary("site-addzero-tool-json").get())
      implementation(libs.findLibrary("io-ktor-ktor-client-websockets").get())
    }
  }
}
