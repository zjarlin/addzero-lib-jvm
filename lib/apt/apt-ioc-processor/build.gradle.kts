
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("site.addzero.gradle.plugin.kspplugin-convention")
}
val libs = versionCatalogs.named("libs")


dependencies {
    implementation(libs.findLibrary("site-addzero-ioc-core").get())
    testImplementation(libs.findLibrary("site-addzero-ioc-core").get())
    testAnnotationProcessor(project(":lib:apt:apt-ioc-processor"))
}
