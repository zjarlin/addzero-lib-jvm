# Spread Pack

`spread-pack` 是一个 Kotlin Compiler Plugin 原型，用现有 Kotlin 注解语法验证 [KT-8214](https://youtrack.jetbrains.com/issue/KT-8214/Allow-kind-of-vararg-but-for-data-class-parameter) 讨论里的两件事：

- 把一个整体参数 carrier 展开成普通命名参数
- 把 `argsof F` 拆成“引用哪个函数参数表”与“如何裁剪这张参数表”

它不提供新语法。当前推荐公开用法是短写法注解，不是裸 `argsof F`。

## 模块

- `kcp-spread-pack-annotations`
  业务代码直接引用的注解定义
- `kcp-spread-pack-plugin`
  真正做 FIR / IR 变换的 compiler plugin
- `kcp-spread-pack-gradle-plugin`
  业务侧优先使用的 Gradle 子插件入口
- `kcp-spread-pack-ide-plugin`
  给 IntelliJ / Android Studio 补派生 overload 与 carrier 字段可见性的 companion plugin

## 接入

业务侧优先走 Gradle 子插件，不手写编译器插件 classpath：

```kotlin
plugins {
    kotlin("jvm") version "<your-kotlin-version>"
    id("site.addzero.kcp.spread-pack") version "<spread-pack-version>"
}
```

这个子插件会自动加上：

- `site.addzero:kcp-spread-pack-annotations`
- `site.addzero:kcp-spread-pack-plugin`

如果你只装 compiler plugin，不装 IDE companion plugin：

- 命令行编译可以过
- 但 IDE 里对生成 overload 和 `@SpreadPackCarrierOf` 派生字段的解析可能仍然发红

## 仓库内最快体验

示例工程：

- `example/example-spread-pack`

推荐直接用仓库脚本：

```bash
./scripts/run-example-spread-pack.sh run
```

当前输出：

```text
TextProps[text,color,maxLines,softWrap,onTextLayout]=(hello,blue,2,false,callback)|Text(text=[MyText] world,color=red,maxLines=3,softWrap=true,layout=wrapped-layout)
```

当前这个 example 的重点不是手写 `data class` carrier，而是：

- 先定义一个模拟第三方库的扁平函数 `vendor.Text(...)`
- 再用空 class `TextProps` 通过 `@SpreadPackCarrierOf("...Text")` 直接借它的完整参数表
- 再让 `MyText(@SpreadPack props: TextProps)` 吃整张参数表

这个脚本会：

- `ADDZERO_USE_INCLUDED_BUILD=false`
- 先检查 `mavenLocal` 里是否已经有 spread-pack 的三个产物
- 再进入 `example/example-spread-pack` 执行 `clean test run`

如果你只想检查本地依赖是否齐全：

```bash
./scripts/run-example-spread-pack.sh check
```

如果你只想跑测试：

```bash
./scripts/run-example-spread-pack.sh test
```

如果脚本提示缺少 `mavenLocal` 产物，再手动发布：

```bash
./gradlew --configure-on-demand \
  :lib:kcp:spread-pack:kcp-spread-pack-annotations:publishToMavenLocal \
  :lib:kcp:spread-pack:kcp-spread-pack-plugin:publishToMavenLocal \
  :lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin:publishToMavenLocal
```

如果这条 publish 路径报 `checkouts/build-logic` checkout conflict，先清理那个 checkout 里的本地改动。

## 最短用法

### 1. 本地 carrier 展开

```kotlin
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack

data class Options(
    val first: Int = 0,
    val second: String = "",
    val third: Boolean = true,
)

@GenerateSpreadPackOverloads
fun render(
    @SpreadPack
    options: Options,
): String = "${options.first}:${options.second}:${options.third}"
```

编译后可以直接这样调：

```kotlin
render()
render(1)
render(second = "x", third = false)
```

这里“展开运算符”的等价物就是 `@SpreadPack + 生成 overload`。不是 `...` 语法，但语义已经覆盖。

### 2. 复用另一函数的整体参数表，并排除字段

```kotlin
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadArgsOf
import site.addzero.kcp.spreadpack.SpreadPack

data class BaseArgs(
    val title: String = "",
    val count: Int = 0,
    val debug: Boolean = false,
    val onDone: (() -> String)? = null,
)

@GenerateSpreadPackOverloads
fun renderBase(
    @SpreadPack
    args: BaseArgs,
): String = TODO()

data class WrapperArgs(
    val title: String = "",
    val count: Int = 0,
    val onDone: (() -> String)? = null,
)

@GenerateSpreadPackOverloads
fun renderWrapper(
    @SpreadPack
    @SpreadArgsOf(
        "site.addzero.example.renderBase",
        parameterTypes = [BaseArgs::class],
        exclude = ["debug"],
    )
    args: WrapperArgs,
): String = TODO()
```

这表示：

- 先引用 `renderBase`
- 选中 `renderBase(BaseArgs)` 这个 definite overload
- 再把这组参数整体传上来
- 最后排除 `debug`

`整体参数传递 + 可排除字段` 当前已经支持。

## 更简化的 carrier 用法

如果你根本不想手写 `BaseArgs` 这种实体类，而是只想“借用已有函数的参数表”，当前推荐这样写：

```kotlin
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf

@SpreadPackCarrierOf(
    "site.addzero.example.renderBase",
    parameterTypes = [BaseArgs::class],
    exclude = ["debug", "onDone"],
)
class RenderAliasArgs

@GenerateSpreadPackOverloads
fun renderAlias(
    @SpreadPack
    args: RenderAliasArgs,
): String = renderBase(
    title = args.title,
    count = args.count,
    debug = true,
)
```

这就是现在最推荐的“少声明一层类型”的公开写法：

- 只声明一个空 class
- 在 class 上标 `@SpreadPackCarrierOf(...)`
- 在函数参数上继续用 `@SpreadPack`

注意当前生成出来的 carrier 不是 data class 主构造器模型，而是：

- 一个无参构造器
- 一组生成属性

所以重点能力是：

- `renderAlias(count = 3)` 这类展开调用
- `args.title` / `args.count` 这类字段访问

不是“自动帮你生成一个完整 data class 主构造器”。

## Compose Text 案例

### 1. 二次封装你自己的 `BaseText`

```kotlin
data class BaseTextArgs(
    val text: String,
    val maxLines: Int,
    val overflow: TextOverflow = TextOverflow.Clip,
)

@Composable
@GenerateSpreadPackOverloads
fun BaseText(
    @SpreadPack
    args: BaseTextArgs,
) {
    Text(
        text = args.text,
        maxLines = args.maxLines,
        overflow = args.overflow,
    )
}
```

再往上包一层：

```kotlin
data class TitleTextArgs(
    val text: String,
    val maxLines: Int,
)

@Composable
@GenerateSpreadPackOverloads
fun TitleText(
    @SpreadPack
    @SpreadArgsOf(
        "site.addzero.demo.BaseText",
        parameterTypes = [BaseTextArgs::class],
        exclude = ["overflow"],
    )
    args: TitleTextArgs,
) {
    BaseText(
        text = args.text,
        maxLines = args.maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
```

### 2. 不定义 `BaseTextArgs`，直接借原生 `Text` 参数表

如果你的目标就是“类似 `Text.$props`，但源码里没有现成实体类”，当前 compiler plugin 侧推荐用 `@SpreadPackCarrierOf`：

```kotlin
@SpreadPackCarrierOf(
    "androidx.compose.material3.Text",
    parameterTypes = [
        // 按你选中的 Text overload 顺序填写对应参数类型
        // 这里只展示写法，不展开完整 Material3 Text 参数列表
    ],
    exclude = ["overflow", "onTextLayout"],
)
class M3TextArgs

@Composable
@GenerateSpreadPackOverloads
fun MyText(
    @SpreadPack
    args: M3TextArgs,
) {
    Text(
        text = args.text,
        modifier = args.modifier,
        color = args.color,
        maxLines = args.maxLines,
        overflow = TextOverflow.Ellipsis,
        style = args.style,
    )
}
```

上面这段的重点不是逐字可抄，而是模式：

- 没有现成 `TextArgs` 也能引用原函数参数表
- 你可以在自己的 wrapper 里固定掉一部分字段
- 上层 API 只暴露你想保留的字段

实际业务里，更推荐先包一层本地 `BaseText`，因为 Compose 原生 `Text` overload 很多，长期维护更稳。

## IDE 支持

IDE companion plugin 已经有可打包原型，作用是让 IDE 看懂这两类派生符号：

- `@GenerateSpreadPackOverloads` 生成的 overload
- `@SpreadPackCarrierOf` 空 carrier 上派生出来的字段

打包命令：

```bash
./gradlew --configure-on-demand :lib:kcp:spread-pack:kcp-spread-pack-ide-plugin:buildPlugin --stacktrace
```

产物位置：

- `lib/kcp/spread-pack/kcp-spread-pack-ide-plugin/build/distributions/`

当前定位很明确：

- 重点是补 resolve / completion 基础可见性
- 不是完整重做 compiler 侧全部诊断
- 如果 compiler plugin 能编译而 IDE 还没完全提示到位，优先以编译结果为准

## `T::class` 在 commonMain 能不能写

可以。

`parameterTypes = [BaseArgs::class]` 这种 `KClass` 字面量在 `commonMain` 可以写，Android/iOS 共享代码都能编译。这里用到的是编译期类型字面量，不要求你在 commonMain 做 JVM 反射。

## 短写法和显式写法的关系

现在推荐的短写法：

```kotlin
@SpreadArgsOf(
    "site.addzero.example.renderBase",
    parameterTypes = [BaseArgs::class],
)
```

如果你更喜欢显式命名，也仍然可以继续写 `functionFqName = "..."`。

只是下面这套显式建模的语法糖：

```kotlin
@SpreadArgsOf(
    overload = SpreadOverload(
        of = SpreadOverloadsOf("site.addzero.example.renderBase"),
        parameterTypes = [BaseArgs::class],
    ),
)
```

内部语义没变，仍然是：

- `SpreadOverloadsOf` 表示 overload set
- `SpreadOverload` 表示 definite overload
- `SpreadArgsOf` / `SpreadPackCarrierOf` 表示从这组参数里拉平字段

也就是说，复杂场景下你仍然可以退回显式写法；只是日常用法不必先写那层样板。

## 当前覆盖范围

- `@SpreadPack` 展开 carrier：已支持
- 整体参数传递：已支持
- `exclude` 排除字段：已支持
- `selector = ATTRS / CALLBACKS`：已支持
- nested `argsof`：已支持
- overload 歧义诊断：已支持
- `argsof` 循环检测：已支持
- 顶层函数与成员函数：已支持
- 空 carrier + `@SpreadPackCarrierOf`：已支持
- example consumer 工程：已支持
- IDE companion plugin：已有原型

## 当前边界

- 裸 `argsof F` 直接语法：当前做不到，也不准备假装做到了
- 原因不是语义想不清，而是 KCP 无法安全改 Kotlin 语法和 parser
- 所以现在只能用注解承载这套语义
- 泛型 carrier：暂不支持
- receiver / context parameter 目标函数：暂不支持
- `@SpreadPackOf` 还保留着，但当前不建议作为主公开入口；优先用 `@SpreadPackCarrierOf`
- IDE plugin 目前重点是“让 IDE 看懂派生 overload / carrier 字段”，不是完整重做编译器诊断

## 验证命令

编译器测试：

```bash
./gradlew --configure-on-demand :lib:kcp:spread-pack:kcp-spread-pack-plugin:test --stacktrace
```

示例工程：

```bash
./scripts/run-example-spread-pack.sh run
```

IDE plugin 打包：

```bash
./gradlew --configure-on-demand :lib:kcp:spread-pack:kcp-spread-pack-ide-plugin:buildPlugin --stacktrace
```
