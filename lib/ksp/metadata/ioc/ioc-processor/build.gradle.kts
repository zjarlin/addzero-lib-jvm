plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation(projects.lib.ksp.metadata.ioc.iocCore)
//            implementation(libs.kotlinpoet)
        }
    }
}
