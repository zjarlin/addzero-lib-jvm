plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
  implementation(catalogLibs.findLibrary("com-squareup-okhttp3-okhttp").get())
  implementation(catalogLibs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
  implementation(catalogLibs.findLibrary("com-sun-mail-jakarta-mail").get())

  testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
  testImplementation(catalogLibs.findLibrary("com-squareup-okhttp3-mockwebserver").get())
}
