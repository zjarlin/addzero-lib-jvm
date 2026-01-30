plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
    id("site.addzero.gradle.plugin.graalvm-convention")
    application
}

dependencies {
    implementation(libs.hutool.all)
    implementation(libs.mybatis.plus.generator)
    implementation(libs.mybatis.plus.annotation)
    // 添加MyBatis Plus核心依赖，解决StringUtils类缺失问题
    implementation(libs.mybatis.plus.core)
    implementation(libs.velocity.engine.core)
    implementation(libs.mysql.connector.java)
    implementation(libs.tool.yml)
    implementation(libs.tool.cli.repl)
}

application {
    mainClass.set("site.addzero.lib_adaptor.MpGenCliKt")
}
