plugins {
    id("site.addzero.buildlogic.spring.spring-starter")
}
val libs = versionCatalogs.named("libs")


dependencies {
//    implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-sql-kotlin").get())
       implementation(libs.findLibrary("org-babyfish-jimmer-jimmer-spring-boot-starter").get())

}
