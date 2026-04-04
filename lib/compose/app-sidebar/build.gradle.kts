plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:compose-workbench-design"))
        }
    }
}
