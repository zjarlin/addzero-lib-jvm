plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
}
val libs = versionCatalogs.named("libs")

dependencies {
    testImplementation(libs.findLibrary("org-springframework-spring-context").get())
    testImplementation(libs.findLibrary("org-springframework-spring-web").get())
}
