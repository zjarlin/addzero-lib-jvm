plugins {
    `java-library`
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()


tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec>().configureEach {
    // 添加完整的UTF-8编码支持
    jvmArgs("-Dfile.encoding=UTF-8")
    //保证终端cli打印正确
    jvmArgs("-Dsun.stdout.encoding=UTF-8")
    jvmArgs("-Dsun.stderr.encoding=UTF-8")
    jvmArgs("-Dsun.jnu.encoding=UTF-8")
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}
