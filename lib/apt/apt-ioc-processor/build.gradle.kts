
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
//    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}


dependencies {
    implementation(libs.ioc.core)
    testImplementation(libs.ioc.core)
    testAnnotationProcessor(projects.lib.apt.aptIocProcessor)
}
