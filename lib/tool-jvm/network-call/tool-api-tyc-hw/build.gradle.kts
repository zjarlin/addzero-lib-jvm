plugins {
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

dependencies {
    // HTTP客户端
//    implementation(libs.jackson.module.kotlin)
    implementation(libs.fastjson2.kotlin)
    implementation(libs.huawei.java.sdk)

//    implementation(libs.commons.codec)
//    implementation(libs.commons.logging)
    implementation(libs.httpclient) {
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }
    implementation(libs.okhttp) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.okio) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.httpcore)
//    implementation(libs.slf4j.api)
//    implementation(libs.slf4j.simple)

    implementation(libs.bgmprovider) {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }
    implementation(libs.bcprov.jdk15to18)

}
