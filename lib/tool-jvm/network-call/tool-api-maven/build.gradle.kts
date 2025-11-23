plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("site.addzero:tool-curl:0.0.672")
    implementation(libs.okhttp)
    implementation(libs.jackson.module.kotlin)

}
