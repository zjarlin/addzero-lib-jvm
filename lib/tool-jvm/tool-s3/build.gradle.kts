import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
  implementation(libs.findLibrary("software-amazon-awssdk-s3").get())
  implementation(libs.findLibrary("org-slf4j-slf4j-api").get())
  implementation(libs.findLibrary("com-github-ben-manes-caffeine-caffeine").get())
  implementation(libs.findLibrary("site-addzero-tool-common-jvm").get())
}
//version= LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
