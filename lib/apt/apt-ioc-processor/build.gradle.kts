
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("site.addzero.gradle.plugin.kspplugin-convention")
}


dependencies {
    implementation(libs.site.addzero.ioc.core)
    testImplementation(libs.site.addzero.ioc.core)
    testAnnotationProcessor(projects.lib.apt.aptIocProcessor)
}
