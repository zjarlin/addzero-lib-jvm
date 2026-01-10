plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}
dependencies {
    implementation(libs.hutool.core)
    implementation(libs.mybatis.plus.core)
//    implementation(libs.mybatis.plus)
//    implementation(libs.commons.lang3)
//    api(libs.mybatis.auto.wrapper.core)


    api(libs.mybatis.auto.wrapper.core)
//    implementation(libs.spring.expression)

    // SpEL 表达式支持（保持兼容 JDK 8）
    implementation(libs.spring.expression)
    implementation(libs.tool.spel)
//    implementation(libs.tool.spel)
//    implementation(libs.spring.core)
}
//implementation(libs.mybatis.spring.boot.starter)
