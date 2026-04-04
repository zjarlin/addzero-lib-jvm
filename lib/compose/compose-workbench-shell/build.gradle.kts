plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:compose:compose-workbench-design"))
            implementation(project(":lib:compose:compose-native-component-searchbar"))
            implementation(project(":lib:compose:compose-native-component-tree"))
        }
    }
}
