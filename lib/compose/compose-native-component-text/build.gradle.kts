plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.kcp.spread-pack") apply false
}

extra["site.addzero.kcp.spread-pack.annotations-added"] = true
apply(plugin = "site.addzero.kcp.spread-pack")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib:kcp:spread-pack:kcp-spread-pack-annotations"))
            implementation(project(":lib:compose:compose-native-component-autocomplet"))
        }
    }
}
