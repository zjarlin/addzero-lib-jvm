plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:compose:compose-apple-corner"))
                implementation(project(":lib:compose:compose-native-component-button"))
                implementation(project(":lib:compose:compose-native-component-searchbar"))
                implementation(project(":lib:tool-kmp:tool-tree"))
            }
        }
    }
}
