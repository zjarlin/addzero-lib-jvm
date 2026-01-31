plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("site.addzero.gradle.plugin.kotlin-convention") version "+" version "+"

}

dependencies {
    implementation(libs.lsi.core)
    implementation(libs.tool.str)
}
