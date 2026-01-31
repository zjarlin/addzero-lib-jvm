
plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
//    id("site.addzero.gradle.plugin.kspplugin-convention")
}


dependencies {
    implementation(libs.ioc.core)
    testImplementation(libs.ioc.core)
    testAnnotationProcessor(projects.lib.apt.aptIocProcessor)
}
