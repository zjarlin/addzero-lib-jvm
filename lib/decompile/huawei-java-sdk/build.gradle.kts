plugins {
    id("site.addzero.gradle.plugin.java-convention")
}
version = "3.2.6"
dependencies {


    implementation(libs.httpclient)
    implementation(libs.okhttp) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.okio) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.slf4j.api)
    implementation(libs.bcprov.jdk15to18)
    implementation(libs.bgmprovider)

}
