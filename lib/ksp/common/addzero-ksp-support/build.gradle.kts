plugins {
    id("kmp-ksp")
    id("kmp-json")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
        }
        jvmMain.dependencies {
            implementation(libs.pinyin4j)
        }
    }
}
