//subprojects {
////    version = "0.0.651"
//    if (path.startsWith(":lib:")) {
//        apply(plugin = "site.addzero.publish-buddy")
//    }
//}
plugins {
    id("site.addzero.buildlogic.version-buddy") version "0.0.651"
    id("site.addzero.buildlogic.publish.publish-buddy") version "0.0.651"
//    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
