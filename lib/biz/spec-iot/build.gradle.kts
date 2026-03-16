plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}

description = "Generic IoT spec, protocol adapters, and TDengine SQL planning for JVM"

dependencies {
    implementation(libs.com.github.xingshuangs.iot.communication)
    implementation(libs.com.infiniteautomation.modbus4j)
}
