import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
  id("site.addzero.ksp.multireceiver")
  application
}

dependencies {
  testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
}

application {
  mainClass.set("site.addzero.example.MainKt")
}
