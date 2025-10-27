plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation("com.taosdata.jdbc:taos-jdbcdriver:3.0.0")
//    implementation(libs.hutool.core)
    implementation("cglib:cglib:3.3.0")

    // 添加 Kotlin 标准库以解决运行时缺失 kotlin.Result 类的问题
//    implementation(kotlin("stdlib"))
}
