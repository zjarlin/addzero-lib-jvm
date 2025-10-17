package site.addzero.buildlogic.common

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.withType

// Java编译任务编码
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Java执行任务编码
tasks.withType<JavaExec>().configureEach {
    // 添加完整的UTF-8编码支持
    jvmArgs("-Dfile.encoding=UTF-8")
    //保证终端cli打印正确
    jvmArgs("-Dsun.stdout.encoding=UTF-8")
    jvmArgs("-Dsun.stderr.encoding=UTF-8")
    jvmArgs("-Dsun.jnu.encoding=UTF-8")
}

// Javadoc任务编码
tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}
