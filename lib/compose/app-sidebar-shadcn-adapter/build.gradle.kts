plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:compose:app-sidebar"))
            api(project(":lib:compose:shadcn-compose-component"))
        }
    }
}
