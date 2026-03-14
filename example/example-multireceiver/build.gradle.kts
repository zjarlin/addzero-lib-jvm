
plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  application
  id("site.addzero.kcp.multireceiver")
}

dependencies {
  testImplementation(kotlin("test"))
}


application {
  mainClass.set("site.addzero.example.MainKt")
}
