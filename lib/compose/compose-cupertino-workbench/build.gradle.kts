import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
  dependencies {
    implementation(project(":lib:compose:scaffold-spi"))
    implementation(project(":lib:compose:app-sidebar"))
    implementation(libs.findLibrary("io-insert-koin-koin-annotations").get())
    implementation(libs.findLibrary("io-github-robinpcrd-cupertino").get())
    implementation(libs.findLibrary("io-github-robinpcrd-cupertino-adaptive").get())
    implementation(libs.findLibrary("io-github-robinpcrd-cupertino-icons-extended").get())
    implementation(project(":lib:compose:app-sidebar-cupertino-adapter"))
    implementation(project(":lib:compose:compose-native-component-searchbar"))
    implementation(project(":lib:compose:compose-native-component-tree"))

  }
}

version = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
