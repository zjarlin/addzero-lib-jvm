plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
   id("site.addzero.gradle.plugin.kotlin-convention") version "+"

}

dependencies {
    implementation(project(":checkouts:lsi:lsi-core"))
    implementation("site.addzero:tool-str:2026.02.23")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}
