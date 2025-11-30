plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("site.addzero:addzero-tool-json:2025.09.29")
            api("site.addzero:addzero-tool-str:2025.09.30")
        }
        jvmMain.dependencies {
            implementation(libs.pinyin4j)
            implementation("site.addzero:tool-io-codegen:${libs.versions.addzero.lib674.get()}")
        }
    }
}
