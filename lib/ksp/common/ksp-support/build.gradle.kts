plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(projects.lib.toolKmp.toolJson)
            api(projects.lib.toolKmp.toolStr)
        }
        jvmMain.dependencies {
            implementation(libs.pinyin4j)
        }
    }
}
