plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

group = "site.addzero"

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
    }
}
