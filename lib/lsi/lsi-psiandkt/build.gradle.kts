plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
   id("site.addzero.gradle.plugin.kotlin-convention") version "+"
    id("site.addzero.gradle.plugin.intellij-core")  version "2025.12.23"

}
val libs = versionCatalogs.named("libs")

afterEvaluate {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.findLibrary("site-addzero-lsi-core").get())
    implementation(libs.findLibrary("site-addzero-lsi-intellij").get())
    api(project(":lib:lsi:lsi-k2"))
    api(libs.findLibrary("site-addzero-lsi-psi").get())
}

