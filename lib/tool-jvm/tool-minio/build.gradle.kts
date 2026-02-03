plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.minio)
    implementation(libs.hutool.core)
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
