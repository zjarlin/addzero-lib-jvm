plugins {
    id("java-library")
    // 暂时注释掉，因为插件需要先发布才能在同一个项目中使用
    // id("site.addzero.apt-buddy")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // PostgreSQL JDBC 驱动（可选，根据实际数据库类型调整）
    implementation("org.postgresql:postgresql:42.7.2")

    // MySQL JDBC 驱动（可选）
     implementation("mysql:mysql-connector-java:8.0.33")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 配置 APT Buddy 插件示例（插件应用后才能生效）
// 插件发布后，取消注释上面的插件应用语句和下面的配置
/*
aptBuddy {
    mustMap.apply {
        // 数据库连接配置
        put("jdbcDriver", "org.postgresql.Driver")
        put("jdbcUrl", "jdbc:postgresql://localhost:5432/my_database")
        put("jdbcUsername", "postgres")
        put("jdbcPassword", "postgres")
        
        // 字典表配置
        put("dictTableName", "sys_dict")
        put("dictIdColumn", "id")
        put("dictCodeColumn", "dict_code")
        put("dictNameColumn", "dict_name")
        
        // 字典项表配置
        put("dictItemTableName", "sys_dict_item")
        put("dictItemForeignKeyColumn", "dict_id")
        put("dictItemCodeColumn", "item_value")
        put("dictItemNameColumn", "item_text")
        
        // 生成的枚举类包名
        put("enumOutputPackage", "com.example.generated.enums")
    }
    
    // 禁用生成 Java 配置类（因为 APT 处理器已经有自己的配置处理方式）
    settingContext.set(
        site.addzero.gradle.plugin.aptbuddy.SettingContextConfig(
            enabled = false
        )
    )
}
*/
