plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}
version = "3.2.6"
dependencies {


    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.squareup.okhttp3:okhttp:4.11.0") {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation("com.squareup.okio:okio:3.5.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78")
    implementation("org.openeuler:bgmprovider:1.0.6")

}
