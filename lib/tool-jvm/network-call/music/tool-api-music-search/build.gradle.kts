plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get())
  implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())


  implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
  implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-serialization-json").get())
  implementation(libs.findLibrary("unbescape").get())

  // Ktor Client
  implementation(libs.findLibrary("io-ktor-ktor-client-core").get())
  implementation(libs.findLibrary("io-ktor-ktor-client-cio").get())
  implementation(libs.findLibrary("io-ktor-ktor-client-content-negotiation").get())
  implementation(libs.findLibrary("io-ktor-ktor-serialization-kotlinx-json").get())
  implementation(libs.findLibrary("io-ktor-ktor-client-logging").get())

  // Ktorfit (Retrofit风格的Ktor接口定义)
//  implementation("de.jensklingenberg.ktorfit:ktorfit-lib:1.11.0")

  // OkHttp (备用)
//  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  // Crypto
//  implementation("org.bouncycastle:bcprov-jdk18on:1.77")

}
