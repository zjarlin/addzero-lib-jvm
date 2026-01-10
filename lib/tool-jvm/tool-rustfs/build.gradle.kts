plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.s3)
    implementation(libs.slf4j.api)
    implementation(libs.caffeine)
    implementation(libs.addzero.tool.common.jvm)
}
