plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:tool-kmp:ktor:starter:starter-spi"))
    implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
    implementation(libs.findLibrary("io-ktor-ktor-server-swagger").get())
}
