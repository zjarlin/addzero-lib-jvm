
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
//    id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
}


dependencies {
    implementation("site.addzero:ioc-core:2025.12.23")
    testImplementation("site.addzero:ioc-core:2025.12.23")
    testAnnotationProcessor(projects.lib.apt.aptIocProcessor)
}
