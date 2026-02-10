plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}
version = "3.2.6"
dependencies {


    implementation(libs.org.apache.httpcomponents.httpclient)
    implementation(libs.com.squareup.okhttp3.okhttp) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.com.squareup.okio.okio) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.org.slf4j.slf4j.api)
    implementation(libs.org.bouncycastle.bcprov.jdk15to18)
    implementation(libs.org.openeuler.bgmprovider)

}
