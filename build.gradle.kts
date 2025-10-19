subprojects {
//    version = "0.0.647"
    if (path.startsWith(":lib:")) {
        apply(plugin = "site.addzero.publish-buddy")
    }
}
plugins {
//    id("site.addzero.buildlogic.version-buddy") version "+"
    id("site.addzero.buildlogic.version-buddy") version "0.0.647"
    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
