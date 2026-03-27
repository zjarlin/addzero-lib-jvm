plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

description = "Generic IoT spec, protocol adapters, and TDengine SQL planning for JVM"

dependencies {
    testRuntimeOnly(libs.findLibrary("org-junit-platform-junit-platform-launcher").get())
}
