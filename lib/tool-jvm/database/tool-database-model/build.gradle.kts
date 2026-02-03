plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
//    id("kmp-json")
}

dependencies {
    // 添加Spring JDBC依赖用于数据库操作
    compileOnly(libs.spring.jdbc)
    compileOnly(libs.spring.context)
//    implementation(libs.hutool.all)
}
