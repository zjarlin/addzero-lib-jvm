plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("kmp-json")
}
val libs = versionCatalogs.named("libs")

dependencies {
    api(libs.findLibrary("com-hivemq-hivemq-mqtt-client").get())
}

