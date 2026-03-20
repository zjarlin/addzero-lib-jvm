plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    application
    id("site.addzero.kcp.i18n")
}

i18n {
    targetLocale.set("en")
    resourceBasePath.set("i18n")
}

application {
    mainClass.set("site.addzero.example.MainKt")
}
