plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
    id("site.addzero.kcp.spread-pack") apply false
}

extra["site.addzero.kcp.spread-pack.annotations-added"] = true
apply(plugin = "site.addzero.kcp.spread-pack")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib:kcp:spread-pack:kcp-spread-pack-annotations"))
                implementation(project(":lib:tool-kmp:tool-enum"))
                implementation(project(":lib:tool-kmp:tool-regex"))
                implementation(project(":lib:tool-kmp:tool-str"))
                implementation(project(":lib:compose:compose-native-component-button"))
                implementation(project(":lib:compose:compose-native-component-tree"))
            }
        }
    }
}
