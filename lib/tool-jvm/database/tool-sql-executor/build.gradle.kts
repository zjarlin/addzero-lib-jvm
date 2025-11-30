plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    compileOnly("mysql:mysql-connector-java:8.0.33")
    compileOnly("org.postgresql:postgresql:42.3.3")
    compileOnly("com.oracle.database.jdbc:ojdbc8:21.5.0.0")
    compileOnly("com.microsoft.sqlserver:mssql-jdbc:10.2.0.jre8")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("org.xerial:sqlite-jdbc:3.39.3.0")
    compileOnly("com.dameng:DmJdbcDriver18:8.1.2.141")
    compileOnly("cn.com.kingbase:kingbase8:8.6.0")
    compileOnly("com.huawei.gauss:gaussdb-jdbc:4.9.0")
    compileOnly("com.oceanbase:oceanbase-client:2.4.5")
    compileOnly("com.aliyun:polardb-jdbc:1.0.0")
    compileOnly("io.tidb:tidb-jdbc:0.1.0")
    compileOnly("com.ibm.db2:jcc:11.5.8.0")
    compileOnly("com.sybase:jconn4:7.0")
}