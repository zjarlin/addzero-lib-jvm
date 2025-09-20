import com.google.devtools.ksp.gradle.KspAATask
import site.addzero.gradle.constant.Disposable.SOURCE_DIR_KMP

plugins {
    id("kmp-app")
    id("kmp-koin")
    id("kmp-json-withtool")
    alias(libs.plugins.composeHotReload)
    id("ksp4projectdir")
    id("kmp-ksp-plugin")
    id("addzero-component")
    id("kmp-config")
}


dependencies {
    kspCommonMainMetadata(projects.lib.ksp.route.addzeroRouteProcessor)
    kspCommonMainMetadata(projects.lib.compose.addzeroComposePropsProcessor)
    kspCommonMainMetadata(projects.lib.ksp.metadata.ioc.addzeroIocProcessor)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.metadata.ioc.addzeroIocCore)
            implementation(projects.shared)
            implementation(projects.sharedCompose)

            implementation(projects.lib.toolKmp.addzeroTool)


            implementation(projects.lib.toolKmp.addzeroNetworkStarter)
            implementation(projects.lib.toolJvm.jimmer.addzeroJimmerModelLowquery)

            // 组件库依赖
            implementation(projects.lib.compose.addzeroComposeNativeComponent)

            //jdbc元数据抽取
//            implementation(projects.lib.ksp.common.addzeroKspSupportJdbc)
            implementation(projects.lib.compose.addzeroComposeNativeComponentHook)

            // 原来的 FileKit 依赖现在由 klibs-component 模块提供
            implementation(libs.filekit.compose)

            //自定义三方组件库
            implementation(projects.lib.compose.addzeroComposeKlibsComponent)


            //注解处理器核心包
            implementation(projects.lib.ksp.route.addzeroRouteCore)
            implementation(projects.lib.compose.addzeroComposePropsAnnotations)

            implementation(projects.lib.ksp.metadata.entity2form.addzeroEntity2formCore)

            // 协程相关依赖
            implementation(libs.kotlinx.coroutines.core)
            //时间依赖
            implementation(libs.kotlinx.datetime)

            // 通用UI组件
            implementation(libs.multiplatform.markdown.renderer.m3)
            implementation(libs.navigation.compose)

            // 图片加载库现在由 klibs-component 模块提供
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            //拖拽库
            implementation(libs.compose.dnd)

            //富文本 see https://klibs.io/project/MohamedRejeb/compose-rich-editor
            implementation(libs.richeditor.compose)

            implementation(compose.materialIconsExtended)


        }

        // 添加源代码集配置，使KSP能够访问依赖模块的源代码
//        commonMain {
//            val addzeroComposeNativeComponent = projects.lib.compose.addzeroComposeNativeComponent
//            val get = addzeroComposeNativeComponent.path
//            val resolve = File(get).resolve(SOURCE_DIR_KMP)
//            kotlin.srcDir(resolve.absolutePath)
//        }
    }
}

// build.gradle.kts
//kotlin {
//    compilerOptions {
//        freeCompilerArgs.add("-Xcontext-parameters")
//    }
//}


// 为所有KSP任务添加对主KSP任务的依赖
tasks.withType<KspAATask>().configureEach {

    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
//        dependsOn(":lib:compose:addzero-compose-native-component-table-pro:kspCommonMainKotlinMetadata")
    }
    if (name != "kspKotlin") {
        dependsOn(":backend:model:kspKotlin")
        dependsOn(":backend:server:kspKotlin")
    }
}
