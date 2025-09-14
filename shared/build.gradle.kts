import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    id("kmp-lib")
    id("kmp-datetime")
    id("kmp-json")

    id("ksp4jdbc")
    id("ksp4enum")
    id("ksp4projectdir")
    id("com.codingfeline.buildkonfig") version "+"
}

buildkonfig {
    packageName = Vars.packageName
    // default config is required
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "name", "value")
    }

//    targetConfigs {
//        // names in create should be the same as target names you specified
//        create("android") {
//            buildConfigField(STRING, "name2", "value2")
//            buildConfigField(STRING, "nullableField", "NonNull-value", nullable = true)
//        }
//
//        create("ios") {
//            buildConfigField(STRING, "name", "valueForNative")
//        }
//    }
}








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
