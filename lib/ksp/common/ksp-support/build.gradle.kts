plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
    id("site.addzero.gradle.plugin.kmp-json-withtool-convention")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(libs.addzero.tool.json)
            api(libs.addzero.tool.str)
        }
        jvmMain.dependencies {
            implementation(libs.pinyin4j)
            implementation(libs.tool.io.codegen)
        }
    }
}
