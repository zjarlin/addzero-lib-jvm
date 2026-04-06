import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

buildscript {
  configurations.classpath {
    // IntelliJ Marketplace publishing loads plugin uploader classes from the root buildscript
    // classloader, so we pin OkHttp/Okio here to avoid old 3.x transitive jars leaking in.
    resolutionStrategy.force(
      "com.squareup.okhttp3:okhttp:4.12.0",
      "com.squareup.okio:okio:3.6.0",
      "com.squareup.okio:okio-jvm:3.6.0",
    )
  }
}

plugins {
//    id("site.addzero.gradle.plugin.version-buddy") version "2025.11.32"
//    alias(libs.plugins.addzeroVersionBuddy)

  alias(libs.plugins.site.addzero.gradle.plugin.publish.buddy)
//    alias(libs.plugins.addzeroPublishBuddyNew)
  alias(libs.plugins.kotlinJvm) apply false
}
//afterEvaluate {
subprojects {
  //  val versionStr = providers.gradleProperty("version").orNull ?: now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
//  val groupId = providers.gradleProperty("group").orNull ?: rootProject.group.toString()
//  group = groupId
  version = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
//    version = "2026.02.02"
  println("项目版本为$version")
  if (path.startsWith(":checkouts:")) {
    apply(plugin = "site.addzero.gradle.plugin.publish-buddy")
  }

//  val shouldUseJava11ForPublishedKspPlugins =
//    path == ":lib:gradle-plugin:project-plugin:gradle-ksp-consumer-base" || (path.startsWith(":lib:ksp:") && name.endsWith("-gradle-plugin"))

//  if (shouldUseJava11ForPublishedKspPlugins) {
//    plugins.withId("org.jetbrains.kotlin.jvm") {
//      extensions.configure(JavaPluginExtension::class.java) {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//        toolchain.languageVersion.set(JavaLanguageVersion.of(11))
//      }
//      extensions.configure(KotlinJvmProjectExtension::class.java) {
//        jvmToolchain(11)
//      }
//      tasks.withType(KotlinCompile::class.java).configureEach {
//        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
//      }
//    }
//  }
}
//}
