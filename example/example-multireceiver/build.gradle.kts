import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("site.addzero.buildlogic.jvm.jvm-ksp-plugin")
  application
}

dependencies {
  implementation(project(":lib:kcp:multireceiver:kcp-multireceiver-annotations"))
  ksp(project(":lib:ksp:metadata:multireceiver-processor"))
  testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
}

application {
  mainClass.set("site.addzero.example.MainKt")
}
