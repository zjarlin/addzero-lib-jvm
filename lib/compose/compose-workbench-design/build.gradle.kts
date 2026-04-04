plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:shadcn-compose-component"))
        }
    }
}
