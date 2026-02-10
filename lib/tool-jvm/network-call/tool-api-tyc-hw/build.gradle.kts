plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    // HTTP客户端
//    implementation(libs.com.fasterxml.jackson.module.jackson.module.kotlin)
    implementation(libs.com.alibaba.fastjson2.fastjson2.kotlin)
    implementation(libs.site.addzero.huawei.java.sdk)

//    implementation(libs.commons.codec.commons.codec)
//    implementation(libs.commons.logging.commons.logging)
    implementation(libs.org.apache.httpcomponents.httpclient) {
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }
    implementation(libs.com.squareup.okhttp3.okhttp) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.com.squareup.okio.okio) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.org.apache.httpcomponents.httpcore)
//    implementation(libs.org.slf4j.slf4j.api)
//    implementation(libs.org.slf4j.slf4j.simple)

    implementation(libs.org.openeuler.bgmprovider) {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }
    implementation(libs.org.bouncycastle.bcprov.jdk15to18)

}
