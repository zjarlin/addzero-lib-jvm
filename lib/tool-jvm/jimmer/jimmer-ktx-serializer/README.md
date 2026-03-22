# Jimmer KTX Serializer

为 Jimmer 实体提供 `kotlinx.serialization` 运行时适配，避免为每个实体重复编写 `UserSerializer`、`DeptSerializer` 这类样板代码。

- Maven coordinate: `site.addzero:jimmer-ktx-serializer`
- Local module path: `lib/tool-jvm/jimmer/jimmer-ktx-serializer`

## What It Does

- 提供通用 `JimmerKtxEntitySerializer<T>`
- 保持 Jimmer 动态对象语义，只输出 `loaded` 且可见的属性
- 支持实体嵌套、关联列表、常见 JVM 标量类型
- 提供 `Json` 与 `SerializersModule` 扩展，便于直接注册多个实体

## Minimal Usage

```kotlin
val json = Json { ignoreUnknownKeys = true }

val content = json.encodeJimmerToString(user)
val parsed = json.decodeJimmerFromString<User>(content)
```

如果要放进 `@Serializable` DTO 中：

```kotlin
@Serializable
data class UserEnvelope(
  @Contextual val user: User,
  @Contextual val dept: Department?,
)

val json = Json {
  serializersModule = jimmerKtxSerializersModule(
    User::class,
    Department::class,
  )
}
```

## Runtime Notes

- 这是 JVM 模块，依赖 Jimmer 运行时元数据
- 反序列化实体时，缺失字段会保持为“未加载”，不会被强行补默认值
- 对于顶层 `entity -> json` / `json -> entity`，不需要逐个手写 `Serializer`
