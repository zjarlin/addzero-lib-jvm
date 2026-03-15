package site.addzero.buildlogic.spring

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")
val springBootVersion = libs.findVersion("spring-boot").get().requiredVersion

dependencies {
    implementation(platform(libs.findLibrary("org-springframework-boot-spring-boot-dependencies").get()))
    testImplementation(libs.findLibrary("org-springframework-boot-spring-boot-starter-test").get())
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter-api").get())
    testImplementation(libs.findLibrary("com-h2database-h2").get())
    testRuntimeOnly(libs.findLibrary("org-junit-jupiter-junit-jupiter-engine").get())
    testImplementation(libs.findLibrary("org-springframework-boot-spring-boot-starter-web").get())
}
