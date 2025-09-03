plugins {
    id("kmp-component")
    id("kmp-json")
//    id("kmp-koin")
}
kotlin {
    sourceSets {
        commonMain.dependencies {

            implementation(projects.lib.toolKmp.addzeroKotlinxSerializationExt)
            implementation(projects.lib.toolKmp.addzeroTool)
            api(projects.lib.compose.addzeroComposeModelComponent)
            api(projects.lib.toolJvm.jimmer.addzeroJimmerModelLowquery)
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
