plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":lib:compose:app-sidebar"))
        }
    }
}
