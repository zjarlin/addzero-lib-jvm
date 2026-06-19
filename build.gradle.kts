import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
//    id("site.addzero.gradle.plugin.version-buddy") version "2025.11.32"
//    alias(libs.plugins.addzeroVersionBuddy)

  alias(libs.plugins.site.addzero.gradle.plugin.publish.buddy)
//    alias(libs.plugins.addzeroPublishBuddyNew)
  alias(libs.plugins.kotlinJvm) apply false
    kotlin("plugin.spring") version "2.4.0"
}
//afterEvaluate {
subprojects {
  val now = LocalDateTime.now()
//    val versionStr = now.format(DateTimeFormatter.ofPattern("yyyy.MM.ddHHmm"))
  val versionStr = now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
  version = versionStr
  if (path == ":lib:tool-jvm:yudao:yudao-spring-boot-starter-captcha") {
    version = "2026.06.17"
  }
  if (path == ":lib:tool-jvm:tool-jackson" || path == ":lib:tool-jvm:tool-jackson-extra") {
    version = "2026.06.19"
  }
  if (path.startsWith(":lib:tool-jvm:yudao3:")) {
    version = "2026.06.17"
  }
//    version = "2026.02.02"
  println("项目版本为$version")
  if (path.startsWith(":checkouts:")) {
    apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
  }
}
//}
repositories {
    mavenCentral()
}
