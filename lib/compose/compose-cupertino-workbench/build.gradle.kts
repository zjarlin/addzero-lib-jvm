plugins {
  id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
  dependencies {
    implementation(libs.findLibrary("scaffold-spi").get())
    implementation(libs.findLibrary("app-sidebar").get())
    implementation(libs.findLibrary("io-insert-koin-koin-annotations").get())
    implementation(libs.findLibrary("io-github-robinpcrd-cupertino").get())
    implementation(libs.findLibrary("io-github-robinpcrd-cupertino-adaptive").get())
    implementation(libs.findLibrary("io-github-robinpcrd-cupertino-icons-extended").get())
    implementation(libs.findLibrary("app-sidebar-cupertino-adapter").get())
    implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
    implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())

  }
}
