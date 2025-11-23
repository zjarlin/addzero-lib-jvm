// =========================================================
// APT Buddy 插件使用示例
// =========================================================
// 这是一个独立的配置示例文件，展示如何在项目中使用 apt-buddy 插件
// 将此配置复制到实际项目的 build.gradle.kts 中使用

plugins {
    id("java-library")
    id("site.addzero.apt-buddy") version "2025.11.27"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // APT 处理器依赖
    annotationProcessor("site.addzero:apt:2025.11.27")
    
    // 数据库驱动（根据实际情况选择）
    implementation("org.postgresql:postgresql:42.7.2")
    // 或者 MySQL
    // implementation("mysql:mysql-connector-java:8.0.33")
}

// 配置 APT Buddy 插件
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
    
    // 可选：禁用生成 Java 配置类（默认启用）
    settingContext.set(
        site.addzero.gradle.plugin.aptbuddy.SettingContextConfig(
            enabled = false
        )
    )
}

// =========================================================
// 使用方式：
// =========================================================
// 1. 执行任务生成配置和打印 Maven pom.xml：
//    ./gradlew generateAptScript
//
// 2. 编译项目（自动应用 APT 配置）：
//    ./gradlew compileJava
//
// 3. 查看控制台输出的 Maven pom.xml 配置，复制到 Maven 项目使用
// =========================================================
