# Compose Props Processor

Compose Props Processor 是一个基于 KSP (Kotlin Symbol Processing) 的代码生成工具，用于为 Compose 函数自动生成辅助工具集，包括状态管理类、remember 函数和 Widget 函数等。

## 功能特性

- **自动生成状态类**：为带注解的 Compose 函数生成响应式状态管理类
- **自动生成 remember 函数**：生成用于创建和记住状态的便捷函数
- **自动生成 Widget 函数**：生成接受状态类作为参数的 Widget 函数
- **智能类型处理**：正确处理函数类型、可空类型、泛型等复杂类型
- **优化的内存使用**：自动识别不需要响应式更新的参数（如函数类型、Modifier 等）
- **内联函数支持**：完整支持 inline 和 reified 泛型函数

## 使用方法

### 1. 添加依赖

在你的 `build.gradle.kts` 文件中添加 KSP 插件和依赖：

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

commonMain.dependencies {
    implementation("site.addzero:addzero-compose-props-annotations:+")
}

dependencies {
    kspCommonMainMetadata("site.addzero:addzero-compose-props-processor:+")
}


```

### 2. 标记需要生成辅助工具的 Compose 函数

使用 `@ComposeAssist` 注解标记你的 Compose 函数：

```kotlin
@Composable
@ComposeAssist
fun MyText(
    text: String,
    color: Color = Color.Black,
    fontSize: TextUnit = 14.sp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        modifier = modifier.clickable { onClick?.invoke() }
    )
}
```

### 3. 构建项目

运行构建命令，KSP 处理器会自动生成辅助工具：

```bash
./gradlew build
```

### 4. 使用生成的辅助工具

生成的代码将包含以下内容：

1. **状态类** (`MyTextState`)：用于管理组件状态
2. **remember 函数** (`rememberMyTextState`)：用于创建和记住状态
3. **Widget 函数** (`MyTextWidget`)：接受状态类作为参数的组件

```kotlin
@Composable
fun MyScreen() {
    // 使用 remember 函数创建状态
    val state = rememberMyTextState(
        text = "Hello World",
        color = Color.Blue,
        onClick = { println("Clicked!") }
    )
    
    // 使用 Widget 函数渲染组件
    MyTextWidget(state = state)
    
    // 或者直接修改状态
    Button(onClick = { 
        state.text = "Updated Text"
        state.color = Color.Red
    }) {
        Text("Update")
    }
}
```

## 高级用法

### 排除特定参数

使用 `@AssistExclude` 注解可以排除不需要包含在状态管理中的参数：

```kotlin
@Composable
@ComposeAssist
fun MyComponent(
    data: List<String>,
    @AssistExclude modifier: Modifier = Modifier,
    @AssistExclude key: String = "default"
) {
    // 实现
}
```

### 内联函数支持

支持内联函数和 reified 泛型参数：

```kotlin
@Composable
@ComposeAssist
inline fun <reified T> MyGenericComponent(
    items: List<T>,
    noinline renderItem: @Composable (T) -> Unit
) {
    // 实现
}
```

### 可空函数类型处理

正确处理可空函数类型参数：

```kotlin
@Composable
@ComposeAssist
fun MyComponent(
    onClick: (() -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null
) {
    // 实现
}
```

## 生成的代码说明

### 状态类

为每个带注解的函数生成对应的状态类，包含：

- 构造函数参数：接收所有需要管理的参数
- MutableState 属性：为需要响应式更新的参数创建状态
- 公开属性：提供 get/set 访问器

示例生成的状态类：

```kotlin
class MyTextState(
    text: String,
    color: Color = Color.Black,
    fontSize: TextUnit = 14.sp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    private val _text = mutableStateOf(text)
    private val _color = mutableStateOf(color)
    private val _fontSize = mutableStateOf(fontSize)
    private val _onClick = onClick  // 函数类型不包装
    private val _modifier = modifier  // Modifier 不包装
    
    var text: String
        get() = _text.value
        set(value) { _text.value = value }
        
    var color: Color
        get() = _color.value
        set(value) { _color.value = value }
        
    var fontSize: TextUnit
        get() = _fontSize.value
        set(value) { _fontSize.value = value }
        
    val onClick: (() -> Unit)?  // 函数类型使用 val
        get() = _onClick
        
    val modifier: Modifier  // Modifier 使用 val
        get() = _modifier
}
```

### Remember 函数

生成便捷的 remember 函数，用于创建和记住状态对象：

```kotlin
@Composable
fun <T, C> rememberMyTextState(
    text: String,
    color: Color = Color.Black,
    fontSize: TextUnit = 14.sp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
): MyTextState {
    return remember {
        MyTextState(
            text = text,
            color = color,
            fontSize = fontSize,
            onClick = onClick,
            modifier = modifier
        )
    }
}
```

### Widget 函数

生成接受状态类作为参数的 Widget 函数，自动展开状态属性：

```kotlin
@Composable inline fun <reified T, C> MyTextWidget(
    state: MyTextState
) {
    MyText(
        text = state.text,
        color = state.color,
        fontSize = state.fontSize,
        onClick = state.onClick,
        modifier = state.modifier
    )
}
```

## 优化特性

### 智能参数处理

自动生成的代码会智能处理不同类型的参数：

1. **函数类型参数**：不包装在 `mutableStateOf` 中，使用 `val` 声明
2. **Compose 内置类型**：如 `Modifier`、`Color` 等不常用的响应式更新参数也不包装
3. **普通数据类型**：包装在 `mutableStateOf` 中，支持响应式更新

### 内存优化

通过避免不必要的 `mutableStateOf` 包装，减少了内存使用并提高了性能。

### 内联函数支持

完整支持内联函数和 reified 泛型参数，生成的 Widget 函数会保持原始函数的 inline 和 reified 修饰符。

## 配置选项


## 支持的类型

- 基本数据类型 (String, Int, Boolean, Double, Float 等)
- 函数类型 (包括 @Composable 函数)
- 泛型类型
- 可空类型
- Compose 内置类型 (Modifier, Color, TextUnit 等)
- 内联函数和 reified 泛型

## 注意事项

1. 确保使用 `@Composable` 注解标记 Compose 函数
2. 确保函数参数有明确的类型声明
3. 对于复杂的自定义类型，可能需要手动提供默认值处理逻辑
4. 内联函数需要使用 `noinline` 修饰符标记函数类型参数以避免编译错误

## 故障排除

### 常见问题

1. **没有生成代码**：确保添加了 KSP 插件和依赖，并重新构建项目
2. **类型错误**：检查函数参数是否有明确的类型声明
3. **导入问题**：确保生成的代码有正确的包导入
4. **内联函数问题**：确保函数类型参数使用了 `noinline` 修饰符

### 日志查看

构建时可以通过查看 KSP 处理器日志来诊断问题：

```bash
./gradlew build --info
```

## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目。

## 许可证

[Apache License 2.0](LICENSE)
