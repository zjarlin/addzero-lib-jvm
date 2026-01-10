plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    api("site.addzero:lsi-apt:2026.01.11")
    api("site.addzero:lsi-core:2026.01.11")
    implementation(libs.tool.str)
}
