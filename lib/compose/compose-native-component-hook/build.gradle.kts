plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
//    id("addzero-component")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("site-addzero-compose-native-component-autocomplet").get())
            implementation(libs.findLibrary("site-addzero-compose-native-component-select").get())
        }
    }
}
// build.gradle.kts
//tasks {
//    compileKotlinWasmJs {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//
//    // 当所有任务都注册后再配置依赖关系
//    afterEvaluate {
//        tasks.matching { it.name.contains("SourcesJar", true) }.configureEach {
//            dependsOn("kspCommonMainKotlinMetadata")
//        }
//    }
//}
