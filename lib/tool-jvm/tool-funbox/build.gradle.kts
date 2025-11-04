plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.hutool.all)
//    implementation(libs.jackson.module.kotlin)
    compileOnly(libs.fastjson2.kotlin)
    implementation("io.swagger:swagger-annotations:1.6.12")

    implementation("site.addzero:tool-reflection:${libs.versions.addzero.lib.get()}")
}
