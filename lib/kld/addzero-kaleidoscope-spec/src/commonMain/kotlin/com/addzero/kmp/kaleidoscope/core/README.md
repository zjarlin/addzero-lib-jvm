# KLD 自动导入功能

KLD (Kaleidoscope) 提供了自动导入功能，可以自动分析类型元素并收集需要导入的类型。

## 功能特性

1. **自动收集导入**：从类型元素中自动收集所有需要导入的类型
2. **黑名单过滤**：支持配置黑名单来过滤不需要导入的类型
3. **包名转换**：支持配置包名转换规则
4. **泛型支持**：自动处理泛型类型参数
5. **数组支持**：自动处理数组组件类型
6. **通配符支持**：自动处理通配符边界类型

## 使用方法

### 基本使用

```kotlin
val typeElement: KldTypeElement = // 获取类型元素
val imports = typeElement.autoImport()
```

### 使用黑名单过滤

```kotlin
val config = KldAutoImportConfig(
    blacklistPredicate = { qualifiedName ->
        // 过滤掉标准库类型
        qualifiedName.startsWith("java.lang.") || 
        qualifiedName.startsWith("kotlin.")
    }
)

val imports = typeElement.autoImport(config)
```

### 使用包名转换

```kotlin
val config = KldAutoImportConfig(
    packageConverter = { qualifiedName ->
        // 将internal包转换为api包
        if (qualifiedName.contains(".internal.")) {
            qualifiedName.replace(".internal.", ".api.")
        } else {
            qualifiedName
        }
    }
)

val imports = typeElement.autoImport(config)
```

### 组合使用

```kotlin
val config = KldAutoImportConfig(
    blacklistPredicate = { qualifiedName ->
        // 过滤掉标准库类型
        qualifiedName.startsWith("java.lang.") || 
        qualifiedName.startsWith("java.util.") ||
        qualifiedName.startsWith("kotlin.")
    },
    packageConverter = { qualifiedName ->
        // 将internal包转换为api包
        if (qualifiedName.contains(".internal.")) {
            qualifiedName.replace(".internal.", ".api.")
        } else {
            qualifiedName
        }
    }
)

val imports = typeElement.autoImport(config)
```

## 收集的类型

自动导入功能会收集以下类型的引用：

1. **父类类型**
2. **实现的接口类型**
3. **字段类型**
4. **方法返回类型**
5. **方法参数类型**
6. **方法抛出的异常类型**
7. **构造函数参数类型**
8. **属性类型**
9. **属性getter/setter相关类型**
10. **泛型参数类型**
11. **数组组件类型**
12. **通配符边界类型**

## 配置选项

### KldAutoImportConfig

| 属性 | 类型 | 描述 |
|------|------|------|
| blacklistPredicate | `(String) -> Boolean` | 黑名单过滤谓词，返回true表示需要过滤掉该导入 |
| packageConverter | `(String) -> String` | 包名转换函数，可以将某些包名转换为其他包名 |

## 注意事项

1. 自动导入功能只收集当前类型直接引用的类型，不会递归收集嵌套类型的引用
2. 默认情况下不会过滤任何类型，如需过滤请配置`blacklistPredicate`
3. 默认情况下不会转换包名，如需转换请配置`packageConverter`
4. 收集的导入是全限定名形式，不包含import关键字