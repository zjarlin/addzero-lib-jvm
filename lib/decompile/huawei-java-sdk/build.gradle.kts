plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
}
val libs = versionCatalogs.named("libs")

version = "3.2.6"
dependencies {


    implementation(libs.findLibrary("org-apache-httpcomponents-httpclient").get())
    implementation(libs.findLibrary("com-squareup-okhttp3-okhttp").get()) {
        exclude(group = "com.squareup.okio", module = "okio")
    }
    implementation(libs.findLibrary("com-squareup-okio-okio").get()) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
    implementation(libs.findLibrary("org-slf4j-slf4j-api").get())
    implementation(libs.findLibrary("org-bouncycastle-bcprov-jdk15to18").get())
    implementation(libs.findLibrary("org-openeuler-bgmprovider").get())

}
