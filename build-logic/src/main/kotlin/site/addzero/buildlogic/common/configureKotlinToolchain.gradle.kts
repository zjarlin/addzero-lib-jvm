//package site.addzero.buildlogic.common
//
//import org.gradle.api.plugins.JavaPluginExtension
//import org.gradle.jvm.toolchain.JavaLanguageVersion
//import org.gradle.kotlin.dsl.configure
//import site.addzero.gradle.AdzeroExtension
//
//plugins {
//    id("site.addzero.buildlogic.common.ext")
//}
//
//val value = the<AdzeroExtension>()
//
//val javaVersion = value.jdkVersion.get().toInt()
//
//extensions.configure<KotlinPluginExtension> {
//
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(javaVersion))
//    }
//}
