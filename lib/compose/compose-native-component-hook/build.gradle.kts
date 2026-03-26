plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
    id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
//    id("addzero-component")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.compose.composeNativeComponent)
           implementation (projects.lib.compose.composeNativeComponentSelect)
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
