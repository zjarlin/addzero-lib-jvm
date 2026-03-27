plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
//  implementation("com.microsoft.playwright:playwright:1.45.0")
  implementation(project(":lib:tool-jvm:network-call:browser:tool-api-browser-automation"))
  implementation(project(":lib:tool-jvm:network-call:tool-api-temp-mail"))
  implementation(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())

}
