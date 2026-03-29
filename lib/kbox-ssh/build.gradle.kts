plugins {
    id("site.addzero.buildlogic.jvm.jvm-koin")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(project(":lib:kbox-core"))
    implementation(libs.findLibrary("com-hierynomus-sshj").get())
}
