# Gradle APT Buddy Plugin

APT Buddy 是一个 Gradle 插件，用于简化 Java 注解处理器 (APT) 的配置，并提供强类型参数传递功能。

## 功能特性

- 🚀 简化 APT 编译参数配置
- 📦 自动生成 Java 风格的配置类（getter/setter）
- 🔧 生成 SettingContext 单例类用于访问配置
- 🎯 支持自定义包名和类名
- 📝 自动生成预编译脚本（可选）

## 使用方法

### 1. 应用插件

```kotlin
plugins {
    id("site.addzero.gradle.plugin.apt-buddy") version "+"
}
```

### 2. 配置 APT 参数

```kotlin
aptBuddy {
    // 配置 APT 编译参数
    mustMap.put("output.dir", "build/generated/apt")
    mustMap.put("processor.option", "value")
    mustMap.put("debug.enabled", "true")
    
    // 可选：自定义输出目录
    aptScriptOutputDir.set("build-logic/src/main/kotlin/conventions/generated")
    
    // 可选：生成预编译脚本
    generatePrecompiledScript.set(true)
    
    // 可选：配置生成的 Java 类
    settingContext.set(
        SettingContextConfig(
            contextClassName = "SettingContext",
            settingsClassName = "Settings",
            packageName = "site.addzero.context",
            outputDir = "src/main/java",
            enabled = true
        )
    )
}
```

### 3. 生成的代码

插件会生成两个 Java 类：

#### Settings.java
```java
package site.addzero.context;

public class Settings {
    private String outputDir = "build/generated/apt";
    private String processorOption = "value";
    private String debugEnabled = "true";

    public Settings() {
    }

    public Settings(String outputDir, String processorOption, String debugEnabled) {
        this.outputDir = outputDir;
        this.processorOption = processorOption;
        this.debugEnabled = debugEnabled;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    // ... 其他 getter/setter
}
```

#### SettingContext.java
```java
package site.addzero.context;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SettingContext {
    private static final AtomicReference<Settings> settingsRef = new AtomicReference<>();

    public static Settings getSettings() {
        Settings settings = settingsRef.get();
        return settings != null ? settings : new Settings();
    }

    public static void initialize(Map<String, String> op) {
        Settings settings = new Settings();
        settings.setOutputDir(op.getOrDefault("output.dir", ""));
        settings.setProcessorOption(op.getOrDefault("processor.option", ""));
        settings.setDebugEnabled(op.getOrDefault("debug.enabled", ""));
        settingsRef.compareAndSet(null, settings);
    }
}
```

### 4. 在代码中使用

```java
// 获取配置
Settings settings = SettingContext.getSettings();
String outputDir = settings.getOutputDir();

// 初始化配置（如果需要）
Map<String, String> config = new HashMap<>();
config.put("output.dir", "custom/path");
SettingContext.initialize(config);
```

## 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `mustMap` | `MapProperty<String, String>` | - | APT 编译参数映射 |
| `aptScriptOutputDir` | `Property<String>` | `build-logic/src/main/kotlin/conventions/generated` | 脚本输出目录 |
| `generatePrecompiledScript` | `Property<Boolean>` | `false` | 是否生成预编译脚本 |
| `settingContext.contextClassName` | `String` | `SettingContext` | 生成的 Context 类名 |
| `settingContext.settingsClassName` | `String` | `Settings` | 生成的 Settings 类名 |
| `settingContext.packageName` | `String` | `site.addzero.context` | 生成类的包名 |
| `settingContext.outputDir` | `String` | `src/main/java` | 生成类的输出目录 |
| `settingContext.enabled` | `Boolean` | `true` | 是否启用代码生成 |

## 与 KSP Buddy 的区别

| 特性 | APT Buddy | KSP Buddy |
|------|-----------|-----------|
| 目标处理器 | Java APT | Kotlin KSP |
| 生成代码风格 | Java (getter/setter) | Kotlin (属性) |
| 配置方式 | `JavaCompile` 任务参数 | KSP 配置块 |
| 输出目录默认 | `src/main/java` | `src/main/kotlin` |

## 任务

- `generateAptScript`: 生成 APT 配置脚本和 Java 类

## 示例

```kotlin
aptBuddy {
    mustMap.apply {
        put("entity.package", "com.example.entity")
        put("dao.package", "com.example.dao")
        put("service.package", "com.example.service")
    }
}
```

生成后可以在代码中使用：

```java
Settings settings = SettingContext.getSettings();
String entityPackage = settings.getEntityPackage();
String daoPackage = settings.getDaoPackage();
String servicePackage = settings.getServicePackage();
```

## Maven pom.xml 配置输出

当执行 `generateAptScript` 任务时，插件会在控制台打印对应的 Maven pom.xml 配置：

```bash
./gradlew generateAptScript
```

控制台输出示例：

```
═══════════════════════════════════════════════════════════════
📦 Maven pom.xml 配置等价项
═══════════════════════════════════════════════════════════════

<properties>
    <apt.entity.package>com.example.entity</apt.entity.package>
    <apt.dao.package>com.example.dao</apt.dao.package>
    <apt.service.package>com.example.service</apt.service.package>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-Aentity.package=${apt.entity.package}</arg>
                    <arg>-Adao.package=${apt.dao.package}</arg>
                    <arg>-Aservice.package=${apt.service.package}</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>

═══════════════════════════════════════════════════════════════
```

您可以直接复制这段配置到 Maven 项目的 pom.xml 中使用。
