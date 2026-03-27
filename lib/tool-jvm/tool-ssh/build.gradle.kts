plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("com-hierynomus-sshj").get())
    implementation(libs.findLibrary("org-slf4j-slf4j-api").get())
}
