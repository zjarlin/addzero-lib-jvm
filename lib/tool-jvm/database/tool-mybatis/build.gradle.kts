
plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
dependencies {
//    implementation(libs.cn.hutool.hutool.all)
    compileOnly(libs.com.baomidou.mybatis.plus)
    implementation(libs.site.addzero.mybatis.auto.wrapper)
//    implementation(libs.site.addzero.mybatis.auto.wrapper)
//    implementation(libs.site.addzero.tool.spring)
    implementation(libs.cn.hutool.hutool.core)
    implementation(libs.site.addzero.tool.spctx)
//    compileOnly(projects.)
//    implementation(libs.com.baomidou.mybatis.plus.core)
//    implementation(libs.org.apache.commons.commons.lang3)
}
//implementation(libs.org.mybatis.spring.boot.mybatis.spring.boot.starter)
