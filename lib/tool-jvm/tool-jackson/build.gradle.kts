plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
  kotlin("plugin.spring")
}

val libs = versionCatalogs.named("libs")

dependencies {
    api(platform(libs.findLibrary("org-springframework-boot-spring-boot-dependencies-v2").get()))
    api(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
    implementation(libs.findLibrary("yudao-jackson-datatype-jsr310").get())
    compileOnly(libs.findLibrary("cn-hutool-hutool-extra").get())
    compileOnly(libs.findLibrary("org-springframework-boot-spring-boot-autoconfigure").get())
    compileOnly(libs.findLibrary("org-springframework-spring-context").get())
    compileOnly(libs.findLibrary("org-springframework-boot-spring-boot-starter-web").get())
    testImplementation(libs.findLibrary("cn-hutool-hutool-extra").get())
    testImplementation(libs.findLibrary("org-springframework-spring-context").get())
    testImplementation(libs.findLibrary("org-springframework-boot-spring-boot-starter-web").get())
  implementation(kotlin("stdlib"))
}
repositories {
  mavenCentral()
}
