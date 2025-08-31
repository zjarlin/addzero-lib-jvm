plugins {
    id("kmp-app")
    alias(libs.plugins.composeHotReload)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
        }
    }
}
