plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("site.addzero:ksp-support:2025.09.30")
            implementation("site.addzero:compose-props-annotations:2025.09.30")
        }

    }
}
