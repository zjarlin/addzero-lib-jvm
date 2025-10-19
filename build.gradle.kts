
allprojects {
    version = "0.0.645"
}
// 或者使用setter方法
subprojects {
//    println("aaaaaaaaa$path")
    if (path.startsWith(":lib:")) {
//        println("aaaaaaa$name")
        apply(plugin = "site.addzero.publish-buddy")
    }
}
plugins {
//    id("site.addzero.buildlogic.versions-buddy") version "+"
    alias(libs.plugins.addzeroPublishBuddy) apply false
    alias(libs.plugins.kotlinJvm) apply false
}
