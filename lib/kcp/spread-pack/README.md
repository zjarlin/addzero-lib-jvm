# Spread Pack

`spread-pack` 是一个 KCP 原型插件，用合法 Kotlin 注解语法模拟 `KT-8214` 讨论里的 `dataarg / argsof` 一小段可落地语义。

当前切片只做两件事：

- 把显式 carrier 类型参数展开成普通命名参数
- 用显式 carrier 类型来规避裸函数重载集的不确定性

这意味着它故意不做新的 Kotlin 语法，也不去猜测 `argsof F` 的重载集合。当前版本要求你先把要展开的参数包类型写清楚。

## 最小示例

```kotlin
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack

data class Options(
    val firstParam: Int = 0,
    val secondParam: String = "",
    val thirdParam: Boolean = true,
)

@GenerateSpreadPackOverloads
fun foo(@SpreadPack options: Options): String =
    "${options.firstParam}:${options.secondParam}:${options.thirdParam}"
```

插件会派生出等价的展开重载，使下面这些调用成立：

```kotlin
foo(secondParam = "a", thirdParam = false)
foo(1)
foo()
```

## 当前规则

- carrier 必须是带主构造的普通类
- 当前按主构造参数展开，不扫描任意属性
- `exclude` 只允许排除带默认值的主构造参数
- `selector = ATTRS` 只展开非函数类型参数
- `selector = CALLBACKS` 只展开函数类型参数
- 如果展开后的签名与现有同名函数冲突，当前原型不保证自动改名生成稳定可调用的新 overload
- 裸重载集合的 `argsof F` 语义暂不支持，当前用显式 carrier 类型来表达“已选定的那个 overload”
