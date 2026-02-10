plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(libs.site.addzero.tool.json)
            api(libs.site.addzero.tool.str)
        }
        jvmMain.dependencies {
            implementation(libs.com.belerweb.pinyin4j)
            implementation(libs.site.addzero.tool.io.codegen)
        }
    }
}
