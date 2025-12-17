plugins {
    id("kmp-ksp")
    `maven-publish`
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api("site.addzero:addzero-route-core:2025.09.29")

            implementation("site.addzero:addzero-ksp-support:2025.09.29")
            implementation("com.google.devtools.ksp:symbol-processing-api:2.2.21-2.0.4")
        }
    }
}

group = "site.addzero"
version = "2025.12.22"