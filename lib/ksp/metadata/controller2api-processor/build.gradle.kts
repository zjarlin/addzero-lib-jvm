plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.common.kspSupport)
            implementation(libs.hutool.all)
        }
    }
}
