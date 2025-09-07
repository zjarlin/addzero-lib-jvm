plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
            api(projects.lib.toolKmp.addzeroToolStr)
        }
        jvmMain.dependencies {
            implementation(libs.pinyin4j)
        }
    }
}
