plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("alipay-easysdk").get())
    implementation(libs.findLibrary("wechatpay-java").get())
}
