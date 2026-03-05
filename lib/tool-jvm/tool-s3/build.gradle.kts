import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
dependencies {
  implementation(libs.software.amazon.awssdk.s3)
  implementation(libs.org.slf4j.slf4j.api)
  implementation(libs.com.github.ben.manes.caffeine.caffeine)
  implementation(libs.site.addzero.tool.common.jvm)
}
version= LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
