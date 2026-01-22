plugins {
    id("site.addzero.buildlogic.kmp.platform.kmp-test")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
    }
}
