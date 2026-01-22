plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
    id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
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
