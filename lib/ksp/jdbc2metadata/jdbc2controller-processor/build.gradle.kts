plugins {
    id("kmp-ksp")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.lib.ksp.common.kspSupportJdbc)
            implementation(projects.lib.ksp.common.kspSupport)
            implementation(projects.lib.toolKmp.jdbc.toolJdbcModel)

        }

    }
}
