import java.text.SimpleDateFormat
import java.util.*

val versionDate: String? = SimpleDateFormat("yyyy.MM.dd").format(Date())
version = versionDate.toString()
// 或者使用setter方法
subprojects {
    if (path.startsWith(":lib:") && name.contains("addzero-")) {
        apply(plugin = "site.addzero.publish-buddy")
    }
}

plugins {
//    id("site.addzero.publish-buddy") version "+"
//    gradlePluginPublishBuddy
    alias(libs.plugins.addzeroPublishBuddy) apply false
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
//    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

