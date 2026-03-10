plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
    }
}
