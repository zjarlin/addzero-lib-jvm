plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.metadata.kspDslBuilder.kspDslBuilderCore)
        }
        jvmMain.dependencies {
            implementation(libs.hutool.all)
        }
    }
}
