plugins {
     id("site.addzero.buildlogic.kmp.kmp-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:tool-kmp:tool-enum"))
        }
    }
}
