plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktorfit")
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")

    id("site.addzero.buildlogic.kmp.kmp-json-withtool")

}
val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(project(":lib:tool-kmp:network-starter"))
//        api(projects.lib.apiMusicSpi)
    }
    sourceSets {

    }
}
