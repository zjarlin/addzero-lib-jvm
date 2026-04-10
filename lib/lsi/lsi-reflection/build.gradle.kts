plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
   id("site.addzero.gradle.plugin.kotlin-convention") version "+"

}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("site-addzero-lsi-core").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())
    testRuntimeOnly(libs.findLibrary("org-junit-platform-junit-platform-launcher").get())
}
