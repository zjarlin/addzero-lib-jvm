plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.lib.ksp.route.routeCore)

            implementation(projects.lib.ksp.common.kspSupport)
        }
    }
}
