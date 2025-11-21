plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

dependencies {
    api("mysql:mysql-connector-java:8.0.33")
    api("org.postgresql:postgresql:42.3.3")
    api("com.oracle.database.jdbc:ojdbc8:21.5.0.0")
    api("com.microsoft.sqlserver:mssql-jdbc:10.2.0.jre8")
}