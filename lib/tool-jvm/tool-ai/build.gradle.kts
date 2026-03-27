plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  id("site.addzero.kcp.transform-overload") version "+"

}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("cn-hutool-hutool-core").get())
  implementation(libs.findLibrary("cn-hutool-hutool-http").get())
  implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
  implementation(libs.findLibrary("site-addzero-tool-str").get())
  implementation(libs.findLibrary("site-addzero-tool-jvmstr").get())
//    implementation(libs.findLibrary("site-addzero-tool-jvmstr").get())
  implementation(libs.findLibrary("site-addzero-tool-reflection").get())
//    implementation(libs.findLibrary("site-addzero-tool-str").get())
}
