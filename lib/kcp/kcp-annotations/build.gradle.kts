plugins {
    id("kmp")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
    }
}
