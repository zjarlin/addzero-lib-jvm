plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.lib.ksp.route.addzeroRouteCore)

            implementation(projects.lib.ksp.common.addzeroKspSupport)
        }
    }
}
