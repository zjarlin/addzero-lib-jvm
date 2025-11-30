plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.jimmer.sql.kotlin)
    implementation(libs.hutool.all)
    api("site.addzero:addzero-jimmer-model-lowquery:2025.09.29")

}
