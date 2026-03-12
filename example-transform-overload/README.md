# Transform Overload KCP 插件示例

这是一个展示如何使用 `kcp-transform-overload` Kotlin 编译器插件的示例项目。

## 项目说明

此示例项目现在按“本地 Maven 已发布 Gradle 插件”的方式配置：

- `settings.gradle.kts` 里的 `pluginManagement` 会先查 `mavenLocal()`
- 通过 `id("site.addzero.kcp.transform-overload")` 应用插件
- 注解依赖由 Gradle 子插件自动补入，无需再手写 `annotations` 依赖

## 项目结构

```
example-transform-overload/
├── build.gradle.kts          # 构建配置，应用插件
├── settings.gradle.kts       # 设置
├── src/main/kotlin/
│   └── site/addzero/example/
│       ├── Input.kt          # Input/Draft 接口定义
│       ├── Converters.kt     # @OverloadTransform 转换函数
│       ├── Models.kt         # 实体类
│       ├── Repository.kt     # @GenerateTransformOverloads 接口
│       └── Main.kt           # 入口程序
└── src/test/kotlin/
    └── site/addzero/example/
        └── TransformOverloadTest.kt  # 测试
```

## 核心概念

### 1. @OverloadTransform

标记类型转换函数：

```kotlin
@OverloadTransform
fun <E : Any> Input<E>.toEntityInput(): E = toEntity()

@OverloadTransform
fun <E : Any> Draft<E>.fromDraft(): E = toEntity()
```

### 2. @GenerateTransformOverloads

标记需要生成重载的接口或函数：

```kotlin
@GenerateTransformOverloads
interface UserRepository {
    fun save(entity: User): User
    fun saveAll(entities: Iterable<User>): List<User>
}
```

编译后自动生成的方法：
- `save(value: Input<User>)` - 自动调用 `toEntityInput()`
- `save(value: Draft<User>)` - 自动调用 `fromDraft()`
- `saveAllViaToEntityInput(values: Iterable<Input<User>>)`
- `saveAllViaFromDraft(values: Iterable<Draft<User>>)`

## 运行示例

```bash
# 从项目根目录执行

# 编译（插件会在此阶段生成重载方法）
./gradlew :example-transform-overload:compileKotlin

# 运行
./gradlew :example-transform-overload:run

# 测试
./gradlew :example-transform-overload:test
```

## 使用场景

此插件特别适用于类似 Jimmer 这样的 ORM 框架，可以：

1. 自动生成接受 `Input<E>` 类型参数的重载方法
2. 自动生成接受 `Draft<E>` 类型参数的重载方法
3. 减少样板代码，提高开发效率

## 注意事项

1. `pluginManagement` 必须写在 `settings.gradle.kts`，不能写在 `build.gradle.kts`
2. 转换函数必须是扩展函数或 `TransformProvider` 中的方法
3. 接口/类级别标记会为所有方法生成重载
4. 函数级别标记只针对单个方法
5. 生成的重载方法名会根据转换函数名进行命名，例如 `ViaToEntityInput`
