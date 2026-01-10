plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sshj)
    implementation(libs.slf4j.api)
}
