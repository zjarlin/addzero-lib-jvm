plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

description = "Generic IoT spec, protocol adapters, and TDengine SQL planning for JVM"

dependencies {
    testRuntimeOnly(libs.org.junit.platform.junit.platform.launcher)
}
