import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension

plugins {
    id("kmp-lib")
    id("kmp-datetime")
    id("kmp-json")
    id("ksp4jdbc")
    id("ksp4enum")
    id("ksp4projectdir")
//    id("kmp-config")
}

//buildkonfig {
//    defSettings(BuildSettings::class)
//}


//fun BuildKonfigExtension.defSettings(klass: kotlin.reflect.KClass<*>) {
//    val configContextByClass = getConfigContextByClass(klass)
//    println("tttttt$configContextByClass")
//    configContextByClass.forEach { (name, value, ktType) ->
//        defaultConfigs("dev") {
//            // 根据 ktType 确定字段类型和值的处理方式
//            when (ktType) {
//                String::class -> {
//                    buildConfigField(FieldSpec.Type.STRING, name, "\"$value\"")
//                }
//
//                Int::class -> {
//                    buildConfigField(FieldSpec.Type.INT, name, value.toString())
//                }
//
//                Float::class -> {
//                    buildConfigField(FieldSpec.Type.FLOAT, name, "${value}f")
//                }
//
//                Long::class -> {
//                    buildConfigField(FieldSpec.Type.LONG, name, "${value}L")
//                }
//
//                Boolean::class -> {
//                    buildConfigField(FieldSpec.Type.BOOLEAN, name, value.toString().lowercase())
//                }
//
//                else -> {
//                    // 处理未知类型，可根据需要添加日志或抛出异常
//                    println("Unsupported type: $ktType for field $name")
//                }
//            }
//        }
//    }
//
//}



dependencies {
    kspCommonMainMetadata(projects.lib.ksp.jdbc2metadata.addzeroJdbc2enumProcessor)
//    kspCommonMainMetadata(projects.lib.ksp.metadata.addzeroApiproviderProcessor)
//    kspCommonMainMetadata(projects.lib.ksp.metadata.addzeroEnumProcessor)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation(libs.ktorfit.lib)

            implementation(libs.kotlinx.datetime)

            implementation(projects.lib.ksp.route.addzeroRouteCore)
            implementation(projects.lib.toolKmp.addzeroTool)
            implementation(projects.lib.toolSpring.starter.addzeroDictTransCore)
            implementation(projects.lib.compose.addzeroComposeModelComponent)
            implementation(projects.lib.toolJvm.jimmer.addzeroJimmerModelLowquery)

            implementation(projects.lib.toolKmp.addzeroNetworkStarter)

            implementation(libs.ktorfit.lib)

        }
    }
}
