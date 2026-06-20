plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(projects.lib.toolJvm.toolCmd)
    implementation(libs.findLibrary("cn-hutool-hutool-core").get())
    implementation(libs.findLibrary("cn-hutool-hutool-system").get())
    implementation(libs.findLibrary("site-addzero-tool-str").get())
}
