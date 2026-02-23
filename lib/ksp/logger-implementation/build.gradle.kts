plugins {
    id("site.addzero.buildlogic.jvm.jvm-ksp")
    id("site.addzero.buildlogic.ksp.ksp-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib:ksp:logger-api"))
//    compileOnly("com.google.auto.service:auto-service-annotations:1.1.0")
//    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
}
