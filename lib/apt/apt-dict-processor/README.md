# APT Dict Processor - 字典枚举代码生成器

从数据库字典表自动生成 Java 枚举类的 APT（Annotation Processing Tool）处理器。

## 特性

- 🚀 **Kotlin 实现** - 使用现代化的 Kotlin 语言重写，代码更简洁
- 🔧 **强类型配置** - 支持通过 `DictProcessorConfig` 进行类型安全的配置
- 📦 **Gradle APT Buddy 集成** - 可与 gradle-apt-buddy 插件配合使用，提供更好的开发体验
- 🔌 **兼容性** - 同时支持驼峰命名和点号分隔两种参数格式
- 🎯 **自定义输出** - 支持指定枚举类的输出目录
- 🌐 **多数据库支持** - 支持 MySQL、PostgreSQL 等主流数据库

## 版本要求

- Java 8+
- Kotlin 2.1.20+

## 使用方法

### 1. Maven 配置

#### 添加依赖

```xml
<dependency>
    <groupId>site.addzero</groupId>
    <artifactId>apt-dict-processor</artifactId>
    <version>2025.11.27</version>
    <scope>provided</scope>
</dependency>
```

#### 配置 Maven Compiler Plugin

```xml
<properties>
    <!-- 数据库连接配置 -->
    <jdbc.driver>com.mysql.cj.jdbc.Driver</jdbc.driver>
    <jdbc.url>jdbc:mysql://192.168.1.140:3306/iot_db?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;useSSL=true&amp;serverTimezone=GMT%2B8</jdbc.url>
    <jdbc.username>root</jdbc.username>
    <jdbc.password>your_password</jdbc.password>
    
    <!-- 字典表配置 -->
    <dict.table.name>sys_dict_type</dict.table.name>
    <dict.id.column>dict_type</dict.id.column>
    <dict.code.column>dict_type</dict.code.column>
    <dict.name.column>dict_name</dict.name.column>
    
    <!-- 字典项表配置 -->
    <dict.item.table.name>sys_dict_data</dict.item.table.name>
    <dict.item.foreign.key.column>dict_type</dict.item.foreign.key.column>
    <dict.item.code.column>dict_value</dict.item.code.column>
    <dict.item.name.column>dict_label</dict.item.name.column>
    
    <!-- 输出配置 -->
    <enum.output.package>com.zlj.iot.enums.generated</enum.output.package>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <annotationProcessorPaths>
                    <!-- Lombok（如果使用） -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                    <!-- APT 字典枚举生成处理器 -->
                    <path>
                        <groupId>site.addzero</groupId>
                        <artifactId>apt-dict-processor</artifactId>
                        <version>2025.11.27</version>
                    </path>
                    <!-- MySQL 驱动 - APT 处理器需要连接数据库 -->
                    <path>
                        <groupId>mysql</groupId>
                        <artifactId>mysql-connector-java</artifactId>
                        <version>8.0.33</version>
                    </path>
                </annotationProcessorPaths>
                
                <!-- APT 处理器编译参数配置 -->
                <compilerArgs>
                    <!-- 数据库连接配置 -->
                    <arg>-Ajdbc.driver=${jdbc.driver}</arg>
                    <arg>-Ajdbc.url=${jdbc.url}</arg>
                    <arg>-Ajdbc.username=${jdbc.username}</arg>
                    <arg>-Ajdbc.password=${jdbc.password}</arg>
                    
                    <!-- 字典表配置 -->
                    <arg>-Adict.table.name=${dict.table.name}</arg>
                    <arg>-Adict.id.column=${dict.id.column}</arg>
                    <arg>-Adict.code.column=${dict.code.column}</arg>
                    <arg>-Adict.name.column=${dict.name.column}</arg>
                    
                    <!-- 字典项表配置 -->
                    <arg>-Adict.item.table.name=${dict.item.table.name}</arg>
                    <arg>-Adict.item.foreign.key.column=${dict.item.foreign.key.column}</arg>
                    <arg>-Adict.item.code.column=${dict.item.code.column}</arg>
                    <arg>-Adict.item.name.column=${dict.item.name.column}</arg>
                    
                    <!-- 生成的枚举类包名 -->
                    <arg>-Aenum.output.package=${enum.output.package}</arg>
                    <!-- 自定义输出目录（可选，生成到源码目录） -->
                    <arg>-Aenum.output.directory=${project.basedir}/src/main/java</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. Gradle 配置

#### 基础配置

```kotlin
plugins {
    id("java")
}

dependencies {
    // APT 处理器
    annotationProcessor("site.addzero:apt-dict-processor:2025.11.27")
    
    // JDBC 驱动（编译时需要）
    annotationProcessor("mysql:mysql-connector-java:8.0.33")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Ajdbc.driver=com.mysql.cj.jdbc.Driver",
            "-Ajdbc.url=jdbc:mysql://192.168.1.140:3306/iot_db",
            "-Ajdbc.username=root",
            "-Ajdbc.password=your_password",
            "-Adict.table.name=sys_dict_type",
            "-Adict.id.column=dict_type",
            "-Adict.code.column=dict_type",
            "-Adict.name.column=dict_name",
            "-Adict.item.table.name=sys_dict_data",
            "-Adict.item.foreign.key.column=dict_type",
            "-Adict.item.code.column=dict_value",
            "-Adict.item.name.column=dict_label",
            "-Aenum.output.package=com.example.enums.generated"
        )
    )
}
```

#### 使用 Gradle APT Buddy 插件（推荐）

```kotlin
plugins {
    id("java")
    id("site.addzero.gradle.plugin.apt-buddy") version "2025.11.27"
}

dependencies {
    annotationProcessor("site.addzero:apt-dict-processor:2025.11.27")
    annotationProcessor("mysql:mysql-connector-java:8.0.33")
}

aptBuddy {
    mustMap.apply {
        // 是否启用字典 APT 处理器（默认: false）
        put("dict.apt.enabled", "true")
        
        // 数据库连接配置
        put("jdbc.driver", "com.mysql.cj.jdbc.Driver")
        put("jdbc.url", "jdbc:mysql://192.168.1.140:3306/iot_db")
        put("jdbc.username", "root")
        put("jdbc.password", "your_password")
        
        // 字典表配置
        put("dict.table.name", "sys_dict_type")
        put("dict.id.column", "dict_type")
        put("dict.code.column", "dict_type")
        put("dict.name.column", "dict_name")
        
        // 字典项表配置
        put("dict.item.table.name", "sys_dict_data")
        put("dict.item.foreign.key.column", "dict_type")
        put("dict.item.code.column", "dict_value")
        put("dict.item.name.column", "dict_label")
        
        // 输出配置
        put("enum.output.package", "com.example.enums.generated")
        put("enum.output.directory", "$projectDir/src/main/java")
    }
}
```

使用 apt-buddy 插件后，可以运行 `./gradlew generateAptScript` 查看等价的 Maven 配置。

## 配置参数说明

| 参数名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `dict.apt.enabled` | 否 | `false` | **是否启用字典 APT 处理器** ⚡ |
| `jdbc.driver` | 否 | `com.mysql.cj.jdbc.Driver` | JDBC 驱动类名 |
| `jdbc.url` | 是 | - | 数据库连接 URL |
| `jdbc.username` | 是 | - | 数据库用户名 |
| `jdbc.password` | 是 | - | 数据库密码 |
| `dict.table.name` | 否 | `sys_dict_type` | 字典主表名称 |
| `dict.id.column` | 否 | `dict_type` | 字典主表 ID 列名 |
| `dict.code.column` | 否 | `dict_type` | 字典主表代码列名 |
| `dict.name.column` | 否 | `dict_name` | 字典主表名称列名 |
| `dict.item.table.name` | 否 | `sys_dict_data` | 字典项表名称 |
| `dict.item.foreign.key.column` | 否 | `dict_type` | 字典项表外键列名 |
| `dict.item.code.column` | 否 | `dict_value` | 字典项代码列名 |
| `dict.item.name.column` | 否 | `dict_label` | 字典项名称列名 |
| `enum.output.package` | 是 | - | 生成的枚举类包名 |
| `enum.output.directory` | 否 | `target/generated-sources/annotations` | 枚举类输出目录 |

**注意**：所有参数同时支持驼峰命名格式（如 `jdbcDriver`）和点号分隔格式（如 `jdbc.driver`），推荐使用点号格式。

### ⚡ 启用/禁用开关

通过 `dict.apt.enabled` 参数控制是否启用字典 APT 处理器：

```kotlin
aptBuddy {
    mustMap.apply {
        // 启用字典 APT 处理器
        put("dict.apt.enabled", "true")  // 开启
        // put("dict.apt.enabled", "false")  // 关闭（默认）
        
        // ... 其他配置
    }
}
```

**使用场景：**
- 🚫 **开发环境禁用**：开发时不需要连接数据库，设置为 `false` 跳过处理
- ✅ **CI/CD 启用**：构建流水线中设置为 `true` 生成枚举类
- 🔄 **按需切换**：通过环境变量动态控制是否生成

**动态控制示例：**
```kotlin
aptBuddy {
    mustMap.apply {
        // 从环境变量读取，默认为 false
        put("dict.apt.enabled", System.getenv("DICT_APT_ENABLED") ?: "false")
    }
}
```

## 数据库表结构要求

### 字典主表（默认 `sys_dict_type`）

| 列名 | 类型 | 说明 |
|------|------|------|
| `dict_type` | varchar | 字典编码（主键） |
| `dict_name` | varchar | 字典名称 |

### 字典项表（默认 `sys_dict_data`）

| 列名 | 类型 | 说明 |
|------|------|------|
| `dict_type` | varchar | 字典编码（外键） |
| `dict_value` | varchar | 字典项值 |
| `dict_label` | varchar | 字典项标签 |

## 生成的枚举类示例

假设数据库中有以下数据：

**sys_dict_type:**
```
dict_type: sys_user_sex
dict_name: 用户性别
```

**sys_dict_data:**
```
dict_type: sys_user_sex, dict_value: 0, dict_label: 男
dict_type: sys_user_sex, dict_value: 1, dict_label: 女
dict_type: sys_user_sex, dict_value: 2, dict_label: 未知
```

生成的枚举类：

```java
package com.example.enums.generated;

/**
 * 用户性别
 *
 * 数据库字典编码: sys_user_sex
 * 自动生成的枚举类，不要手动修改
 */
public enum EnumSysUserSex {

    _0("0", "男"),
    _1("1", "女"),
    _2("2", "未知");

    private final String code;
    private final String desc;

    EnumSysUserSex(String code, String desc) {
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
     *
     * @param code 编码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static EnumSysUserSex fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (EnumSysUserSex e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 根据描述获取枚举值
     *
     * @param desc 描述
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static EnumSysUserSex fromDesc(String desc) {
        if (desc == null) {
            return null;
        }
        for (EnumSysUserSex e : values()) {
            if (e.desc.equals(desc)) {
                return e;
            }
        }
        return null;
    }
}
```

## 使用生成的枚举

```java
// 通过编码获取枚举
EnumSysUserSex sex = EnumSysUserSex.fromCode("0");
System.out.println(sex.getDesc()); // 输出: 男

// 通过描述获取枚举
EnumSysUserSex sex2 = EnumSysUserSex.fromDesc("女");
System.out.println(sex2.getCode()); // 输出: 1

// 直接使用枚举常量
String maleCode = EnumSysUserSex._0.getCode(); // "0"
String maleDesc = EnumSysUserSex._0.getDesc(); // "男"
```

## 故障排查

### 1. 参数未被识别

**问题**：编译时看到警告 "以下选项未被任何处理程序识别"

**解决**：
- 确保使用点号分隔格式：`-Ajdbc.driver` 而不是 `-AjdbcDriver`
- 检查参数拼写是否正确
- 查看本文档的"配置参数说明"部分

### 2. 数据库连接失败

**问题**：编译时看到 "Communications link failure" 或类似错误

**解决**：
- 检查数据库地址、端口是否正确
- 确认数据库用户名和密码是否正确
- 检查网络连接是否正常
- 确认 JDBC 驱动已添加到 annotationProcessorPaths

### 3. 未生成枚举类

**问题**：编译成功但没有生成枚举类

**解决**：
- 检查数据库表是否有数据
- 查看编译日志，确认处理器是否运行
- 确认 `enum.output.package` 参数已正确设置
- 检查数据库表名和列名配置是否与实际一致

## 技术实现

- **语言**：Kotlin 2.1.20
- **编译目标**：Java 8
- **架构**：
  - `DictProcessorConfig`: 强类型配置类
  - `DictMetadataExtractor`: 数据库元数据提取器
  - `DictEnumCodeGenerator`: 枚举代码生成器
  - `DictEnumProcessor`: APT 主处理器

## 相关项目

- [gradle-apt-buddy](../../gradle-plugin/project-plugin/gradle-apt-buddy) - Gradle APT 参数配置插件
- [dict-trans-spring-boot-starter](https://github.com/zjarlin/addzero-lib-jvm/tree/main/lib/tool-starter/dict-trans-spring-boot-starter) - 字典翻译 Spring Boot Starter

## License

Apache License 2.0
