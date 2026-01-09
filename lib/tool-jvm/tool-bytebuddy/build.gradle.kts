plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  implementation("net.bytebuddy:byte-buddy:1.18.3")
  implementation(project(":lib:tool-jvm:tool-reflection"))
}
