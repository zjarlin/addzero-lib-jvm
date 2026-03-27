plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
//            implementation()


        
    implementation(project(":lib:compose:compose-native-component-autocomplet"))

    implementation(project(":lib:compose:compose-native-component-text"))

    implementation(project(":lib:compose:compose-native-component-toast"))
}
    }
}
