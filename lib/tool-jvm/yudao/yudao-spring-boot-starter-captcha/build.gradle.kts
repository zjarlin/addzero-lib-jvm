plugins {
    id("site.addzero.buildlogic.jvm.java-convention")
    id("site.addzero.gradle.plugin.publish-buddy")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    api(catalogLibs.findLibrary("com-anji-plus-captcha-spring-boot-starter").get())
    api(catalogLibs.findLibrary("cn-hutool-hutool-all").get())

    compileOnly(catalogLibs.findLibrary("org-springframework-spring-web").get())
    compileOnly(catalogLibs.findLibrary("org-springframework-data-spring-data-redis").get())
}
