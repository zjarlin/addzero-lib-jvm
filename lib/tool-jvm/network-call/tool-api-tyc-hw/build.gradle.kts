plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    // HTTP客户端
//    implementation(libs.jackson.module.kotlin)
    implementation(libs.fastjson2.kotlin)
    implementation("site.addzero:huawei-java-sdk:${libs.versions.addzero.lib.get()}")

//    implementation("commons-codec:commons-codec:1.16.0")
//    implementation("commons-logging:commons-logging:1.2")
    implementation("org.apache.httpcomponents:httpclient:4.5.14") {
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }
    implementation("com.squareup.okhttp3:okhttp:4.11.0") {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation("com.squareup.okio:okio:3.5.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation("org.apache.httpcomponents:httpcore:4.4.13")
//    implementation("org.slf4j:slf4j-api:2.0.16")
//    implementation("org.slf4j:slf4j-simple:2.0.16")

    implementation("org.openeuler:bgmprovider:1.0.6") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78")

}
