//subprojects {
////    version = "0.0.652"
//    version = "2025.10.20"
//
//    if (!path.startsWith(":lib:")) {
//        "not publish module ${project.name}, skip"
//        return@subprojects
//    }
//    listOf(
//        "site.addzero.publish-buddy",
////        "site.addzero.buildlogic.publish.publish-buddy",
//    ).forEach {
//        apply(plugin = it)
////        autoApplyPlugin(it)
//    }
//}
subprojects {
    version = "0.0.657"
}
plugins {
//    id("site.addzero.buildlogic.version-buddy") version "0.0.656"
    alias(libs.plugins.addzeroVersionBuddy)
    alias(libs.plugins.addzeroPublishBuddyNew)
//    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
