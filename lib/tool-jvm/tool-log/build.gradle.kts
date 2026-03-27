plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")




dependencies {
    implementation(libs.findLibrary("org-slf4j-slf4j-api").get())
}
