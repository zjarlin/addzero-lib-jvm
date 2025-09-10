plugins {
    id("kmp-lib")
    id("ksp4self")
    id("kmp-ktorfit")
//    id("kmp-koin")
}
dependencies {
    kspCommonMainMetadata(projects.lib.ksp.metadata.addzeroApiproviderProcessor)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("io.github.ltttttttttttt:LazyPeopleHttp-lib:+")

            implementation(projects.lib.toolKmp.addzeroTool)
//            implementation(projects.lib.compose.addzeroComposeNativeComponentTableCore)
            implementation(projects.lib.toolJvm.jimmer.addzeroJimmerModelLowquery)
            implementation(projects.shared)
            implementation(projects.lib.toolKmp.addzeroNetworkStarter)
        }
    }
}
