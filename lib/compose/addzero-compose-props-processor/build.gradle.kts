plugins {
    id("kmp-ksp")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.common.addzeroKspSupport)
            implementation(projects.lib.compose.addzeroComposePropsAnnotations)
        }
        jvmMain.dependencies {
        }

    }
}
