plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.hutool.system)
    implementation("site.addzero:tool-str")
}
