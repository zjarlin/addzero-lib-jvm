# tool-enum

枚举工具模块。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-enum")
```

主要能力（当前位于 JVM 源集）：

- `bitValue`：按枚举 `ordinal` 生成二进制位值。
- `Collection<Enum<*>>.toBitmask()`：将枚举集合转换为二进制掩码。
- `EnumBitmaskUtils`：在 JVM 侧将二进制掩码还原为枚举集合。
