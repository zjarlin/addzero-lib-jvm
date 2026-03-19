plugins {
    id("site.addzero.buildlogic.kmp.kmp-core")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.javazoom.jlayer)
        }
    }
}
