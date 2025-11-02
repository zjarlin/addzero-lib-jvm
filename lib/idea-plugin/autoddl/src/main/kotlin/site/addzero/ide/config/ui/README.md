# 通用设置模块使用指南

## 概述

通用设置模块提供了一个可配置的树形设置面板基类，允许开发者轻松创建自定义名称的设置界面。

## 核心组件

### 1. BaseConfigurableTreeUI

基础的可配置树形设置面板类，支持：

- 自定义显示名称
- 自定义配置扫描逻辑
- 树形结构展示
- 表单生成和管理

### 2. ConfigurableTreeUI

AutoDDL 插件的具体实现，使用默认名称 "AutoDDL 配置"。

## 使用方法

### 基本使用

```kotlin
// 使用默认配置
class MyConfigurableTreeUI : BaseConfigurableTreeUI(
    displayName = "我的插件配置"
) {
    // 可以重写方法来定制行为
}
```

### 自定义扫描器

```kotlin
class MyConfigurableTreeUI : BaseConfigurableTreeUI(
    displayName = "我的插件配置",
    configScanner = {
        // 自定义配置扫描逻辑
        ConfigScanner.scanAndRegisterConfigs()
        // 或者注册其他配置类
        ConfigRegistry.registerConfig(MyConfigClass::class)
    }
)
```

### 完整示例

```kotlin
package my.plugin.config

import site.addzero.ide.config.ui.BaseConfigurableTreeUI
import site.addzero.ide.config.registry.ConfigRegistry
import site.addzero.ide.config.annotation.SettingRoute
import site.addzero.ide.config.annotation.Configurable
import site.addzero.ide.config.annotation.ConfigField
import site.addzero.ide.config.model.InputType

// 1. 定义配置类
@Route("我的设置", "基本配置")
@Configurable
data class MyConfig(
    @ConfigField(
        label = "服务器地址",
        description = "请输入服务器地址",
        required = true
    )
    val serverUrl: String = "http://localhost:8080",
    
    @ConfigField(
        label = "端口",
        description = "服务器端口",
        inputType = InputType.NUMBER
    )
    val port: Int = 8080
)

// 2. 创建设置界面
class MyPluginSettingsUI : BaseConfigurableTreeUI(
    displayName = "我的插件配置",
    configScanner = {
        // 注册配置类
        ConfigRegistry.registerConfig(MyConfig::class)
    }
)

// 3. 在 plugin.xml 中注册
// <applicationConfigurable parentId="tools" instance="my.plugin.config.MyPluginSettingsUI" id="my.plugin.settings">
//     <label>我的插件设置</label>
// </applicationConfigurable>
```

## 自定义配置选项

### 设置面板名称

通过 `displayName` 参数设置显示名称：

```kotlin
BaseConfigurableTreeUI(displayName = "自定义名称")
```

### 配置扫描逻辑

通过 `configScanner` 参数自定义配置扫描逻辑：

```kotlin
BaseConfigurableTreeUI(
    displayName = "我的配置",
    configScanner = {
        // 扫描特定包
        scanPackage("my.package.config")
        // 或者手动注册
        ConfigRegistry.registerConfig(Config1::class)
        ConfigRegistry.registerConfig(Config2::class)
    }
)
```

### 扩展基类

如果需要更复杂的行为，可以继承 `BaseConfigurableTreeUI` 并重写方法：

```kotlin
class AdvancedConfigurableTreeUI : BaseConfigurableTreeUI(
    displayName = "高级配置"
) {
    override fun createTreePanel(): JPanel {
        // 自定义树面板创建逻辑
        return super.createTreePanel()
    }
    
    override fun createConfigPanel(
        configInfo: ConfigRouteInfo,
        formBuilder: DynamicFormBuilder
    ): JPanel {
        // 自定义配置面板创建逻辑
        return super.createConfigPanel(configInfo, formBuilder)
    }
}
```

## 架构设计

### 类层次结构

```
BaseConfigurableTreeUI (基类)
    └── ConfigurableTreeUI (AutoDDL 实现)
    └── 自定义实现类...
```

### 配置流程

1. **配置类定义** - 使用注解定义配置类
2. **配置注册** - 通过扫描器注册配置
3. **树形构建** - 根据路径构建树形结构
4. **表单生成** - 根据配置项生成表单
5. **数据绑定** - 加载和保存配置数据

## 扩展点

### 可重写方法

- `createTreePanel()` - 创建树面板
- `buildPathFromNode()` - 构建节点路径
- `initializeTreeAndPanels()` - 初始化树和面板
- `buildTreeStructure()` - 构建树结构
- `createConfigPanel()` - 创建配置面板
- `isModified()` - 检查是否有修改
- `apply()` - 应用更改
- `reset()` - 重置配置
- `disposeUIResources()` - 清理资源

## 最佳实践

1. **命名规范** - 使用清晰的配置类名称和路径
2. **分组组织** - 使用路径进行逻辑分组
3. **默认值** - 为所有配置项提供合理的默认值
4. **验证** - 实现适当的验证逻辑
5. **持久化** - 实现配置数据的持久化

## 示例配置类

```kotlin
@Route("数据库", "连接配置")
@Configurable
data class DatabaseConfig(
    @ConfigField(
        label = "主机",
        description = "数据库服务器地址",
        required = true
    )
    val host: String = "localhost",
    
    @ConfigSelect(
        label = "数据库类型",
        description = "选择数据库类型",
        options = [
            SelectOption("mysql", "MySQL"),
            SelectOption("postgresql", "PostgreSQL")
        ]
    )
    val dbType: String = "mysql",
    
    @ConfigCheckbox(
        label = "启用SSL",
        description = "使用SSL加密连接"
    )
    val useSSL: Boolean = false
)
```

## 注意事项

1. 确保所有配置类使用 `@Configurable` 注解
2. 配置路径应该清晰易懂
3. 避免过深的树形结构
4. 提供有意义的配置项描述
5. 实现适当的错误处理

