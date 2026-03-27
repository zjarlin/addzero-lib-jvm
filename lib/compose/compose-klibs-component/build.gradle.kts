plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("site.addzero:tool:2025.10.07")
            implementation(project(":lib:compose:compose-model-component"))
//            implementation("com.seanproctor:data-table-material3:0.11.4")
//           implementation("io.github.aleksandar-stefanovic:composematerialdatatable:1.2.1")
            implementation(libs.io.github.vinceglb.filekit.compose)
            implementation(libs.io.coil.kt.coil3.coil.compose)
            implementation(libs.io.coil.kt.coil3.coil.network.ktor3)
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
