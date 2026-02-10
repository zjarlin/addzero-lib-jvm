plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

dependencies {
    implementation(libs.org.owasp.encoder.encoder.esapi)
    compileOnly(libs.mysql.mysql.connector.java)
    compileOnly(libs.org.postgresql.postgresql)
    compileOnly(libs.com.oracle.database.jdbc.ojdbc8)
    compileOnly(libs.com.microsoft.sqlserver.mssql.jdbc)
    compileOnly(libs.com.h2database.h2)
    compileOnly(libs.org.xerial.sqlite.jdbc)
    compileOnly(libs.com.dameng.dm.jdbc.driver18)
    compileOnly(libs.cn.com.kingbase.kingbase8)
    compileOnly(libs.com.oceanbase.oceanbase.client)
//    compileOnly(libs.com.huawei.gauss.gaussdb.jdbc)
//    compileOnly(libs.com.aliyun.polardb.jdbc)
//    compileOnly(libs.io.tidb.tidb.jdbc)
    compileOnly(libs.com.ibm.db2.jcc)
//    compileOnly("com.taosdata:taos-jdbcdriver:3.2.7")

   implementation(libs.com.taosdata.jdbc.taos.jdbcdriver)
//    compileOnly(libs.com.sybase.jconn4)

    testImplementation(libs.com.h2database.h2)
    testImplementation(libs.org.xerial.sqlite.jdbc)
}
