plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
}
