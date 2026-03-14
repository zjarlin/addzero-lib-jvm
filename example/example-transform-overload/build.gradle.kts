plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
  application
  id("site.addzero.kcp.transform-overload")
}


application {
  mainClass.set("site.addzero.example.MainKt")
}
