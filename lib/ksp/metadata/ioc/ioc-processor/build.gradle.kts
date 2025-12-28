plugins {
    id("kmp-ksp")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation(project(":lib:ksp:metadata:ioc:ioc-core"))
            implementation(project(":checkouts:lsi:lsi-ksp"))
        }
    }
}
