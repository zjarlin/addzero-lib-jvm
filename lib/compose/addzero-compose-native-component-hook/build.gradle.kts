plugins {
    id("kmp-component")
    id("kmp-json-withtool")
//    id("addzero-component")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.compose.addzeroComposeNativeComponent)
           implementation (projects.lib.compose.addzeroComposeNativeComponentSelect)
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
