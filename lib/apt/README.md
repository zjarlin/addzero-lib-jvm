# 字典枚举 APT 处理器

从数据库字典表自动生成 Java 风格的枚举类。

## 功能特性

- 从数据库字典表和字典项表读取数据
- 自动生成标准的 Java 枚举类
- 提供 `fromCode()` 和 `fromDesc()` 工具方法
- 支持多种数据库（PostgreSQL、MySQL 等）

## 使用方法

### 1. 添加依赖

在你的项目 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    annotationProcessor(projects.lib.apt) // 或 "site.addzero:apt:版本号"
    
    // 根据你的数据库类型添加 JDBC 驱动
    implementation("org.postgresql:postgresql:42.7.2")
    // 或 MySQL
    // implementation("mysql:mysql-connector-java:8.0.33")
}
```

### 2. 配置编译选项

在 `build.gradle.kts` 中配置数据库连接和表结构信息：

```kotlin
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        // 数据库连接配置
        "-AjdbcDriver=org.postgresql.Driver",
        "-AjdbcUrl=jdbc:postgresql://localhost:5432/your_database",
        "-AjdbcUsername=your_username",
        "-AjdbcPassword=your_password",
        
        // 字典表配置
        "-AdictTableName=sys_dict",
        "-AdictIdColumn=id",
        "-AdictCodeColumn=dict_code",
        "-AdictNameColumn=dict_name",
        
        // 字典项表配置
        "-AdictItemTableName=sys_dict_item",
        "-AdictItemForeignKeyColumn=dict_id",
        "-AdictItemCodeColumn=item_value",
        "-AdictItemNameColumn=item_text",
        
        // 生成的枚举类包名
        "-AenumOutputPackage=com.example.generated.enums"
    ))
}
```

### 3. 数据库表结构

#### 字典表 (sys_dict)
```sql
CREATE TABLE sys_dict (
    id BIGINT PRIMARY KEY,
    dict_code VARCHAR(100),  -- 字典编码，如 "user_status"
    dict_name VARCHAR(200)   -- 字典名称，如 "用户状态"
);
```

#### 字典项表 (sys_dict_item)
```sql
CREATE TABLE sys_dict_item (
    id BIGINT PRIMARY KEY,
    dict_id BIGINT,          -- 外键关联字典表
    item_value VARCHAR(100), -- 字典项编码，如 "ACTIVE"
    item_text VARCHAR(200)   -- 字典项描述，如 "激活"
);
```

### 4. 编译项目

执行编译命令：

```bash
./gradlew compileJava
```

APT 处理器会自动：
1. 连接到数据库
2. 读取字典表和字典项数据
3. 在 `build/generated/sources/annotationProcessor` 目录下生成 Java 枚举类

### 5. 生成的枚举示例

假设数据库中有以下数据：

**sys_dict 表：**
| id | dict_code | dict_name |
|----|-----------|-----------|
| 1  | user_status | 用户状态 |

**sys_dict_item 表：**
| id | dict_id | item_value | item_text |
|----|---------|------------|-----------|
| 1  | 1       | ACTIVE     | 激活      |
| 2  | 1       | INACTIVE   | 未激活    |

生成的枚举类：

```java
package com.example.generated.enums;

/**
 * 用户状态
 *
 * 数据库字典编码: user_status
 * 自动生成的枚举类，不要手动修改
 */
public enum EnumUserStatus {

    ACTIVE("ACTIVE", "激活"),
    INACTIVE("INACTIVE", "未激活");

    private final String code;
    private final String desc;

    EnumUserStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据编码获取枚举值
     */
    public static EnumUserStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (EnumUserStatus e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据描述获取枚举值
     */
    public static EnumUserStatus fromDesc(String desc) {
        if (desc == null) {
            return null;
        }
        for (EnumUserStatus e : values()) {
            if (e.desc.equals(desc)) {
                return e;
            }
        }
        return null;
    }
}
```

### 6. 使用生成的枚举

```java
// 通过编码查找
EnumUserStatus status = EnumUserStatus.fromCode("ACTIVE");
System.out.println(status.getDesc()); // 输出: 激活

// 通过描述查找
EnumUserStatus status2 = EnumUserStatus.fromDesc("激活");
System.out.println(status2.getCode()); // 输出: ACTIVE

// 直接使用
if (user.getStatus().equals(EnumUserStatus.ACTIVE.getCode())) {
    // 用户已激活
}
```

## 配置参数说明

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| jdbcDriver | 否 | org.postgresql.Driver | JDBC 驱动类名 |
| jdbcUrl | **是** | - | 数据库连接 URL |
| jdbcUsername | **是** | - | 数据库用户名 |
| jdbcPassword | **是** | - | 数据库密码 |
| dictTableName | 否 | sys_dict | 字典表名 |
| dictIdColumn | 否 | id | 字典表主键列名 |
| dictCodeColumn | 否 | dict_code | 字典编码列名 |
| dictNameColumn | 否 | dict_name | 字典名称列名 |
| dictItemTableName | 否 | sys_dict_item | 字典项表名 |
| dictItemForeignKeyColumn | 否 | dict_id | 字典项外键列名 |
| dictItemCodeColumn | 否 | item_value | 字典项编码列名 |
| dictItemNameColumn | 否 | item_text | 字典项描述列名 |
| enumOutputPackage | **是** | - | 生成枚举类的包名 |

## 注意事项

1. **数据库连接**：确保编译时数据库可访问
2. **命名转换**：字典编码会自动转换为驼峰命名的枚举类名
3. **类名前缀**：生成的枚举类会自动添加 "Enum" 前缀
4. **重复检测**：如果字典编码转换后的类名重复，会跳过生成并发出警告
5. **空字典项**：没有字典项的字典会被跳过

## 与 KSP 版本的对比

| 特性 | KSP 版本 | APT 版本 |
|------|----------|----------|
| 语言 | Kotlin | Java |
| 生成代码 | Kotlin enum | Java enum |
| 适用项目 | Kotlin/KMP | Java/JVM |
| 配置方式 | kspBuddy DSL | Gradle 编译选项 |
| 拼音转换 | 支持 | 简化版 |

## 故障排除

### 编译时找不到数据库驱动

确保在 `dependencies` 中添加了正确的 JDBC 驱动：

```kotlin
dependencies {
    implementation("org.postgresql:postgresql:42.7.2")
}
```

### 数据库连接失败

检查：
1. 数据库是否启动
2. URL、用户名、密码是否正确
3. 网络是否可达
4. 防火墙设置

### 未生成枚举类

检查：
1. 数据库表中是否有数据
2. 表名和列名配置是否正确
3. 查看编译日志中的警告信息

## 许可证

根据项目主许可证
