plugins {
  id("site.addzero.buildlogic.kmp.kmp-ktorfit")
//    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
  id("site.addzero.buildlogic.kmp.kmp-json-withtool")
  id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
dependencies {
//    kspCommonMainMetadata("io.github.ltttttttttttt:LazyPeopleHttp:2.2.5")
//    kspCommonMainMetadata("io.github.ltttttttttttt:LazyPeopleHttp:2.2.5")
  kspCommonMainMetadata(libs.method.semanticizer.processor)
//  kspCommonMainMetadata(project(":lib:tool-jvm:network-call:music:api-netease-semantic-impl"))
}
version = "2026.02.16"
kotlin {
  dependencies {
    implementation(project(":lib:ksp:metadata:method-semanticizer:method-semanticizer-api"))
    implementation(libs.site.addzero.network.starter)
//    implementation(project(":lib:tool-jvm:network-call:music:api-music-spi"))
  }
//    sourceSets {
//        commonMain.dependencies {
//            implementation("io.github.ltttttttttttt:LazyPeopleHttp-lib:+")
//        }
//        commonTest.dependencies {
//            implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.test)
//        }
//    }
}

