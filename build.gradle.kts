
subprojects {
    version="0.0.646"
//    println("aaaaaaaaa$path")
    if (path.startsWith(":lib:")) {
//        println("aaaaaaa$name")
        apply(plugin = "site.addzero.publish-buddy")
    }
}
plugins {
//    id("site.addzero.buildlogic.version-buddy") version "+"
//    id("site.addzero.buildlogic.version-buddy") version "+"
    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
