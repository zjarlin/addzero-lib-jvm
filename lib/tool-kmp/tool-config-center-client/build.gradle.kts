plugins {
  id("site.addzero.buildlogic.kmp.kmp-ktor-client")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

val libs = versionCatalogs.named("libs")

kotlin {
  sourceSets {
    commonTest.dependencies {
      implementation(libs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
      implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-test").get())
    }
  }
}

tasks.named("jvmTest") {
  val baseUrl = providers.environmentVariable("CONFIG_CENTER_TEST_BASE_URL").orElse("")
  val username = providers.environmentVariable("CONFIG_CENTER_TEST_USERNAME").orElse("")
  val passwordPresent = providers.environmentVariable("CONFIG_CENTER_TEST_PASSWORD")
    .map { it.isNotBlank().toString() }
    .orElse("false")
  inputs.property("configCenterTestBaseUrl", baseUrl)
  inputs.property("configCenterTestUsername", username)
  inputs.property("configCenterTestPasswordPresent", passwordPresent)
  outputs.upToDateWhen { passwordPresent.get() != "true" }
}
