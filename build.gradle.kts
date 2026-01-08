import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
//    id("site.addzero.gradle.plugin.version-buddy") version "2025.11.32"
//    alias(libs.plugins.addzeroVersionBuddy)

    id("site.addzero.gradle.plugin.publish-buddy") version "2025.12.21"
//    alias(libs.plugins.addzeroPublishBuddyNew)
    alias(libs.plugins.kotlinJvm) apply false
}
//afterEvaluate {
subprojects {
    val now = LocalDateTime.now()
//    val versionStr = now.format(DateTimeFormatter.ofPattern("yyyy.MM.ddHHmm"))
    val versionStr = now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    version = versionStr
//    version = "2026.01.03"
    println("项目版本为$versionStr")
    if (path.startsWith(":checkouts:")) {
        apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
    }
}
//}
