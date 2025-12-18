import java.time.LocalDate

plugins {
//    id("site.addzero.gradle.plugin.version-buddy") version "2025.11.32"
//    alias(libs.plugins.addzeroVersionBuddy)

    id("site.addzero.gradle.plugin.publish-buddy") version "2025.12.21"
//    alias(libs.plugins.addzeroPublishBuddyNew)
    alias(libs.plugins.kotlinJvm) apply false
}
//afterEvaluate {
subprojects {
    val now = LocalDate.now().toString().replace("-", ".")
//    version = now
    version = "2025.12.23"
    println("项目版本为$now")
    if (path.startsWith(":checkouts:")) {
        apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
    }
}
//}

