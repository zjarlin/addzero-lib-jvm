plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    implementation(libs.encoder.esapi)
    compileOnly(libs.mysql.connector.java)
    compileOnly(libs.postgresql)
    compileOnly(libs.ojdbc8)
    compileOnly(libs.mssql.jdbc)
    compileOnly(libs.h2)
    compileOnly(libs.sqlite.jdbc)
    compileOnly(libs.dameng.jdbc.driver)
    compileOnly(libs.kingbase8)
    compileOnly(libs.oceanbase.client)
//    compileOnly(libs.gaussdb.jdbc)
//    compileOnly(libs.polardb.jdbc)
//    compileOnly(libs.tidb.jdbc)
    compileOnly(libs.jcc)
//    compileOnly("com.taosdata:taos-jdbcdriver:3.2.7")

   implementation(libs.taos.jdbcdriver)
//    compileOnly(libs.jconn4)

    testImplementation(libs.h2)
    testImplementation(libs.sqlite.jdbc)
}
