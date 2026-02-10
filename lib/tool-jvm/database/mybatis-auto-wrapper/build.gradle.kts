plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
dependencies {
    implementation(libs.cn.hutool.hutool.core)
    implementation(libs.com.baomidou.mybatis.plus.core)
//    implementation(libs.com.baomidou.mybatis.plus)
//    implementation(libs.org.apache.commons.commons.lang3)
//    api(libs.site.addzero.mybatis.auto.wrapper.core)


    api(libs.site.addzero.mybatis.auto.wrapper.core)
//    implementation(libs.org.springframework.spring.expression)

    // SpEL 表达式支持（保持兼容 JDK 8）
    implementation(libs.org.springframework.spring.expression)
    implementation(libs.site.addzero.tool.spel)
//    implementation(libs.site.addzero.tool.spel)
//    implementation(libs.org.springframework.spring.core)
}
//implementation(libs.org.mybatis.spring.boot.mybatis.spring.boot.starter)
