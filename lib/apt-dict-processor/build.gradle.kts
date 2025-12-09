import org.gradle.plugins.signing.Sign

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.apt-buddy") version "2025.11.28"
}


dependencies {
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("mysql:mysql-connector-java:8.0.33")
}


// 配置 APT Buddy 插件（用于测试和示例）
aptBuddy {
    mustMap.apply {
        // 是否启用字典 APT 处理器（默认: false）
        put("dictAptEnabled", "false")

        // 数据库连接配置（使用小驼峰格式）
        put("jdbcDriver", "com.mysql.cj.jdbc.Driver")
        put("jdbcUrl", "jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&connectTimeout=30000&socketTimeout=30000&autoReconnect=true")
        put("jdbcUsername", "root")
        put("jdbcPassword", "zljkj~123")
        // 字典表配置
        put("dictTableName", "sys_dict_type")
        put("dictIdColumn", "dict_type")
        put("dictCodeColumn", "dict_type")
        put("dictNameColumn", "dict_name")
        // 字典项表配置
        put("dictItemTableName", "sys_dict_data")
        put("dictItemForeignKeyColumn", "dict_type")
        put("dictItemCodeColumn", "dict_value")
        put("dictItemNameColumn", "dict_label")
        // 生成的枚举类包名
        put("enumOutputPackage", "site.addzero.apt.test.enums")
        // 可选：自定义输出目录（如果需要生成到源码目录）
         put("enumOutputDirectory", "$projectDir/src/test/java")
    }

    // 启用生成 Java 配置类（Settings 和 SettingContext）
    settingContext.set(
        site.addzero.gradle.plugin.aptbuddy.SettingContextConfig(
            contextClassName = "DictProcessorSettings",
            settingsClassName = "DictProcessorConfig",
            packageName = "site.addzero.apt.config",
            outputDir = "src/main/java",
            enabled = true
        )
    )
}

// 禁用签名任务（临时用于本地发布）
tasks.withType<Sign>().configureEach {
    enabled = false
}
