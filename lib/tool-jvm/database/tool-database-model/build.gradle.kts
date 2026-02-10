plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("kmp-json")
}

dependencies {
    // 添加Spring JDBC依赖用于数据库操作
    compileOnly(libs.org.springframework.spring.jdbc)
    compileOnly(libs.org.springframework.spring.context)
//    implementation(libs.cn.hutool.hutool.all)
}
