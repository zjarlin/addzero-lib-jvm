plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

//java {
//  sourceCompatibility = JavaVersion.VERSION_21
//  targetCompatibility = JavaVersion.VERSION_21
//}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//  compilerOptions {
//    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
//  }
//}
//
dependencies {
  implementation(libs.software.amazon.awssdk.s3)
  implementation(libs.org.slf4j.slf4j.api)
  implementation(libs.com.github.ben.manes.caffeine.caffeine)
  implementation(libs.site.addzero.tool.common.jvm)
}
