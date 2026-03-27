plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(libs.findLibrary("org-owasp-encoder-encoder-esapi").get())
    compileOnly(libs.findLibrary("mysql-mysql-connector-java").get())
    compileOnly(libs.findLibrary("org-postgresql-postgresql").get())
    compileOnly(libs.findLibrary("com-oracle-database-jdbc-ojdbc8").get())
    compileOnly(libs.findLibrary("com-microsoft-sqlserver-mssql-jdbc").get())
    compileOnly(libs.findLibrary("com-h2database-h2").get())
    compileOnly(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
    compileOnly(libs.findLibrary("com-dameng-dm-jdbc-driver18").get())
    compileOnly(libs.findLibrary("cn-com-kingbase-kingbase8").get())
    compileOnly(libs.findLibrary("com-oceanbase-oceanbase-client").get())
//    compileOnly(libs.findLibrary("com-huawei-gauss-gaussdb-jdbc").get())
//    compileOnly(libs.findLibrary("com-aliyun-polardb-jdbc").get())
//    compileOnly(libs.findLibrary("io-tidb-tidb-jdbc").get())
    compileOnly(libs.findLibrary("com-ibm-db2-jcc").get())
//    compileOnly("com.taosdata:taos-jdbcdriver:3.2.7")

   implementation(libs.findLibrary("com-taosdata-jdbc-taos-jdbcdriver").get())
//    compileOnly(libs.findLibrary("com-sybase-jconn4").get())

    testImplementation(libs.findLibrary("com-h2database-h2").get())
    testImplementation(libs.findLibrary("org-xerial-sqlite-jdbc-v3").get())
}
