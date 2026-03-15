package site.addzero.buildlogic.jvm

plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}

val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("org-projectlombok-lombok").get())
    annotationProcessor(libs.findLibrary("org-projectlombok-lombok").get())
}
