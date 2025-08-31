plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
        }
        jvmMain.dependencies {
            api(projects.lib.kld.addzeroKaleidoscopeSpec)
        }
    }
}
