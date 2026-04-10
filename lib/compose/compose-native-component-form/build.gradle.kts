plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
    id("site.addzero.kcp.spread-pack") version "+"
}
val libs = versionCatalogs.named("libs")

extra["site.addzero.kcp.spread-pack.annotations-added"] = true
apply(plugin = "site.addzero.kcp.spread-pack")

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.findLibrary("kcp-spread-pack-annotations").get())
                implementation(libs.findLibrary("tool-enum").get())
                implementation(libs.findLibrary("tool-regex").get())
                implementation(libs.findLibrary("site-addzero-tool-str").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-button").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
            }
        }
    }
}
