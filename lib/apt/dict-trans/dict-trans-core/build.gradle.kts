plugins {
  id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
  api(project(":lib:apt:dict-trans:apt-dict-trans-core"))
}
