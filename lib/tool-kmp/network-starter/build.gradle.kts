plugins {
  id("site.addzero.buildlogic.kmp.kmp-ktor-client")
  id("site.addzero.buildlogic.kmp.kmp-koin-core")
}
val libs = versionCatalogs.named("libs")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile<*>>().configureEach {
  if (name == "compileKotlinWasmJs") {
    incremental = false
  }
}

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation("site.addzero:tool-coll:2026.03.08")
      implementation(libs.findLibrary("de-jensklingenberg-ktorfit-ktorfit-lib").get())
      implementation(libs.findLibrary("site-addzero-tool-json").get())
      implementation(libs.findLibrary("io-ktor-ktor-client-websockets").get())
    }
  }
}
