import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-convention")
    kotlin("jvm")
}

val javaVersion = extensions.getByName<JavaPluginExtension>("java").targetCompatibility.toString()
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
        jvmTarget.set(JvmTarget.fromTarget(javaVersion))
        // 添加UTF-8编码支持
//        freeCompilerArgs.add("-J-Dfile.encoding=UTF-8")
    }
}
