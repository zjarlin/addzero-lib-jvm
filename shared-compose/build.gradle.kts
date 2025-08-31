plugins {
    id("kmp-lib")
    id("ksp4self")
}
dependencies {
    kspCommonMainMetadata(libs.lazy.people.ksp)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation("io.github.ltttttttttttt:LazyPeopleHttp-lib:+")
            implementation(libs.lazy.people.http)
            implementation(projects.lib.toolKmp.addzeroNetworkStarter)
        }
    }
}
