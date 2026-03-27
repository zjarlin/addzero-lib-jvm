plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    // HTTP客户端
//    implementation(libs.findLibrary("com-fasterxml-jackson-module-jackson-module-kotlin").get())
    implementation(libs.findLibrary("com-alibaba-fastjson2-fastjson2-kotlin").get())
    implementation(libs.findLibrary("site-addzero-huawei-java-sdk").get())

//    implementation(libs.findLibrary("commons-codec-commons-codec").get())
//    implementation(libs.findLibrary("commons-logging-commons-logging").get())
    implementation(libs.findLibrary("org-apache-httpcomponents-httpclient").get()) {
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }
    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get()) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.findLibrary("com-squareup-okio-okio").get()) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.findLibrary("org-apache-httpcomponents-httpcore").get())
//    implementation(libs.findLibrary("org-slf4j-slf4j-api").get())
//    implementation(libs.findLibrary("org-slf4j-slf4j-simple").get())

    implementation(libs.findLibrary("org-openeuler-bgmprovider").get()) {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }
    implementation(libs.findLibrary("org-bouncycastle-bcprov-jdk15to18").get())

}
