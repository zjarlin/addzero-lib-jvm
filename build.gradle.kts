import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
//    id("site.addzero.gradle.plugin.version-buddy") version "2025.11.32"
//    alias(libs.plugins.addzeroVersionBuddy)

    alias(libs.plugins.site.addzero.gradle.plugin.publish.buddy)
//    alias(libs.plugins.addzeroPublishBuddyNew)
    alias(libs.plugins.kotlinJvm) apply false
}
//afterEvaluate {
subprojects {
    val now = LocalDateTime.now()
    val versionStr = providers.gradleProperty("version")
        .orNull
        ?: now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HHmm"))
    val groupId = providers.gradleProperty("group")
        .orNull
        ?: rootProject.group.toString()
    group = groupId
    version = versionStr
//    version = "2026.02.02"
    println("项目版本为$versionStr")
    if (path.startsWith(":checkouts:")) {
        apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
    }
}
//}
