plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
    id("site.addzero.gradle.plugin.graalvm-convention")
    application
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("cn-hutool-hutool-all").get())
    implementation(libs.findLibrary("com-baomidou-mybatis-plus-generator").get())
    implementation(libs.findLibrary("com-baomidou-mybatis-plus-annotation").get())
    // 添加MyBatis Plus核心依赖，解决StringUtils类缺失问题
    implementation(libs.findLibrary("com-baomidou-mybatis-plus-core").get())
    implementation(libs.findLibrary("org-apache-velocity-velocity-engine-core").get())
    implementation(libs.findLibrary("mysql-mysql-connector-java").get())
    implementation(libs.findLibrary("site-addzero-tool-yml").get())
    implementation(libs.findLibrary("site-addzero-tool-cli-repl").get())
}

application {
    mainClass.set("site.addzero.lib_adaptor.MpGenCliKt")
}
