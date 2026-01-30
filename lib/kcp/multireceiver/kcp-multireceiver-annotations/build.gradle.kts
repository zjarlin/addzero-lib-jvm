plugins {
    id("site.addzero.gradle.plugin.kmp-test-convention")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
    }
}
