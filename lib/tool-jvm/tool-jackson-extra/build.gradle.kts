plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    api(projects.lib.toolJvm.toolJackson)
    api(libs.findLibrary("org-babyfish-jimmer-jimmer-core").get())
    api(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-datetime").get())
}
