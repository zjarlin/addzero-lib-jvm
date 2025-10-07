
plugins {
//    id("kotlin-convention")

    id("kmp-core")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.lib.toolKmp.toolStr)
        }

    }
}
