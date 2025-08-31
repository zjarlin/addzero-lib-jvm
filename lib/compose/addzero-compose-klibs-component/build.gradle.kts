plugins {
    id("kmp-component")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.toolKmp.addzeroTool)
            implementation(projects.lib.compose.addzeroComposeModelComponent)
//            implementation("com.seanproctor:data-table-material3:0.11.4")

//           implementation("io.github.aleksandar-stefanovic:composematerialdatatable:1.2.1")

            implementation(libs.filekit.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)


        }
    }
}
// build.gradle.kts


tasks {
    compileKotlinWasmJs {
        dependsOn("kspCommonMainKotlinMetadata")
    }

    // 当所有任务都注册后再配置依赖关系
    afterEvaluate {
        tasks.matching { it.name.contains("SourcesJar", true) }.configureEach {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}
