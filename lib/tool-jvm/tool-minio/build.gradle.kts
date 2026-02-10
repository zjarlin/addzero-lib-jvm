plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.io.minio.minio)
    implementation(libs.cn.hutool.hutool.core)
    implementation(libs.org.slf4j.slf4j.api)

    testImplementation(libs.junit.junit.junit.jupiter.api)
    testRuntimeOnly(libs.junit.junit.junit.jupiter.engine)
}
