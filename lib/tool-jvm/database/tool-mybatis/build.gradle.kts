
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
dependencies {
//    implementation(libs.hutool.all)
    compileOnly(libs.mybatis.plus)
    implementation(libs.mybatis.auto.wrapper)
//    implementation(libs.mybatis.auto.wrapper)
//    implementation(libs.addzero.tool.spring)
    implementation(libs.hutool.core)
    implementation(libs.tool.spctx)
//    compileOnly(projects.)
//    implementation(libs.mybatis.plus.core)
//    implementation(libs.commons.lang3)
}
//implementation(libs.mybatis.spring.boot.starter)
