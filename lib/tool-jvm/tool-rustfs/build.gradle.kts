plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    val awsSdkVersion = "2.29.16"
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    implementation(libs.slf4j.api)
    implementation(libs.caffeine)
    implementation("site.addzero:addzero-tool-common-jvm:2025.09.29")
}
