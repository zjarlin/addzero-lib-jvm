plugins {
    id("kmp-lib")
    id("kmp-datetime")
//    id("kmp-ktorfit")
//    id("de.jensklingenberg.ktorfit") version "+"

    id("ksp4jdbc")
    id("ksp4enum")
    id("ksp4projectdir")
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

            implementation(projects.lib.toolKmp.addzeroTool)
            implementation(projects.lib.toolSpring.starter.addzeroDictTransCore)
            implementation(projects.lib.compose.addzeroComposeModelComponent)
            implementation(projects.lib.toolJvm.jimmer.addzeroJimmerModelLowquery)

            implementation(projects.lib.toolKmp.addzeroNetworkStarter)

            implementation(libs.ktorfit.lib)

        }
    }
}
