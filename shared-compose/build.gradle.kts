plugins {
    id("kmp-lib")
    id("ksp4self")
    id("kmp-ktorfit")
}
dependencies {
    kspCommonMainMetadata(projects.lib.ksp.metadata.addzeroApiproviderProcessor)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("io.github.ltttttttttttt:LazyPeopleHttp-lib:+")

            implementation(projects.shared)
            implementation(projects.lib.toolKmp.addzeroNetworkStarter)
        }
    }
}
