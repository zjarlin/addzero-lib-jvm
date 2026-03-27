plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.libs.kmp-datetime")
}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
//                implementation(projects.lib.toolKmp.tool)
                implementation(project(":lib:compose:compose-native-component-button"))
                implementation(project(":lib:compose:compose-native-component-tree"))
            }
        }


    }
}
