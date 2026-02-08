plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
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
