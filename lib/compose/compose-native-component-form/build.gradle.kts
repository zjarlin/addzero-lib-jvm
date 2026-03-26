plugins {
    id("site.addzero.buildlogic.kmp.composition.kmp-component")
    id("site.addzero.buildlogic.kmp.libs.kmp-datetime")
}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
//                implementation(projects.lib.toolKmp.tool)
                implementation(projects.lib.compose.composeNativeComponentButton)
                implementation(projects.lib.compose.composeNativeComponentTree)
            }
        }


    }
}
