# Spread Pack

`spread-pack` 是一个 Kotlin Compiler Plugin 原型，用合法 Kotlin 注解语法去承载 [KT-8214](https://youtrack.jetbrains.com/issue/KT-8214/Allow-kind-of-vararg-but-for-data-class-parameter) 讨论里的两层核心意图：

- 把一个显式 carrier 参数展开成普通命名参数
- 把 `argsof F` 拆成“overload 集合”和“确定 overload”两个可组合操作

它故意不发明新语法，也不假装裸 `argsof F` 一定唯一。当前版本更接近“用 KCP 验证语义边界”而不是“直接定义 Kotlin 语言提案”。

## 当前模块

- `kcp-spread-pack-annotations`
- `kcp-spread-pack-plugin`
- `kcp-spread-pack-gradle-plugin`
- `kcp-spread-pack-ide-plugin`

## 业务项目怎么接

正常接入优先走 Gradle 子插件，不手写 `ksp(...)` 或编译器插件 classpath：

```kotlin
plugins {
    kotlin("jvm") version "<your-kotlin-version>"
    id("site.addzero.kcp.spread-pack") version "<spread-pack-version>"
}
```

子插件会负责：

- 加上 `site.addzero:kcp-spread-pack-annotations`
- 注入编译器插件 artifact
- 让业务侧只写注解，不手动拼 compiler plugin 参数

如果你想让 JetBrains IDE 也理解这些派生 overload，再安装 `kcp-spread-pack-ide-plugin` 打出来的 zip。

## 仓库内最快体验路径

这个仓库里已经有一个真实 consumer example：

- `example/example-spread-pack`

本地直接验证：

```bash
./gradlew -p example/example-spread-pack clean test run --no-configuration-cache
```

当前预期输出：

```text
form:demo:true:-|wrapper:hello:2:done
```

如果要验证发布产物而不是 `includeBuild`：

```bash
./gradlew --no-configuration-cache \
  :lib:kcp:spread-pack:kcp-spread-pack-annotations:publishToMavenLocal \
  :lib:kcp:spread-pack:kcp-spread-pack-plugin:publishToMavenLocal \
  :lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin:publishToMavenLocal

ADDZERO_USE_INCLUDED_BUILD=false \
./gradlew -p example/example-spread-pack clean test run --no-configuration-cache
```

## 三个注解各管什么

- `@GenerateSpreadPackOverloads`
  这是 opt-in 开关。只有打了它，编译器才会为目标函数生成派生 overload。
- `@SpreadPack`
  表示“把这个 carrier 参数按主构造参数展开”。
- `@SpreadArgsOf`
  表示“当前 carrier 不自己定义一套参数语义，而是对齐某个 definite overload 的参数表，并允许继续 `exclude / selector`”。

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

编译器会派生出等价的 forwarding overload，使下面这些调用成立：

```kotlin
foo(secondParam = "a", thirdParam = false)
foo(1)
foo()
```

## `argsof` 的三层表达

当前原型把 `argsof` 拆成三层注解值：

- `SpreadOverloadsOf("pkg.fn")`
  表示函数 `F` 的整个 overload 集合
- `SpreadOverload(...)`
  在 overload 集合里选定一个 definite overload
- `SpreadArgsOf(...)`
  从该 definite overload 的参数表继续拉平字段

这样才能表达用户讨论里的关键点：

- `F` 本身可能是 overload set，不是单个函数
- 只有把 overload set 继续分解，才能得到确定的那个 overload
- `argsof` 作用的对象应该是“被选定的 overload”，而不是模糊的同名函数集合

`SpreadOverloadsOf.functionFqName` 当前支持：

- 顶层函数：`site.addzero.example.renderBase`
- 成员函数：`site.addzero.example.Renderer.renderBase`

## `argsof` 示例

```kotlin
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadArgsOf
import site.addzero.kcp.spreadpack.SpreadOverload
import site.addzero.kcp.spreadpack.SpreadOverloadsOf
import site.addzero.kcp.spreadpack.SpreadPack

data class BaseOptions(
    val title: String = "",
    val count: Int = 0,
    val debug: Boolean = false,
    val onDone: (() -> String)? = null,
)

@GenerateSpreadPackOverloads
fun renderBase(@SpreadPack options: BaseOptions): String = TODO()

fun renderBase(title: String): String = title

data class WrapperArgs(
    val title: String = "",
    val count: Int = 0,
    val onDone: (() -> String)? = null,
)

@GenerateSpreadPackOverloads
fun renderWrapper(
    @SpreadPack
    @SpreadArgsOf(
        overload = SpreadOverload(
            of = SpreadOverloadsOf("site.addzero.example.renderBase"),
            parameterTypes = [BaseOptions::class],
        ),
        exclude = ["debug"],
    )
    args: WrapperArgs,
): String = TODO()
```

这段代码的含义是：

1. 先指向 `renderBase` 这个 overload set
2. 再用 `parameterTypes = [BaseOptions::class]` 选中 `renderBase(BaseOptions)`
3. 再把这个 overload 的参数表拉平
4. 最后在拉平字段集上执行 `exclude = ["debug"]`

不是“把函数对象直接放进语法里”，而是显式建模：

- overload 集合
- definite overload
- definite overload 的参数展开

## 能力覆盖状态

当前原型已经覆盖下面这些场景：

- carrier 展开成普通命名参数：已覆盖
- 引用另一函数的整体参数表再继续传递：已覆盖
- 在整体参数传递时排除字段：已覆盖
- `selector = ATTRS / CALLBACKS`：已覆盖
- nested `argsof`：已覆盖
- overload set 歧义诊断：已覆盖
- `argsof` 循环检测：已覆盖
- 成员函数 overload set：已覆盖
- Gradle 真实 consumer 工程：已覆盖
- IDE 侧派生 overload 解析：已有原型实现

当前自动化验证来源：

- 编译器集成测试 `SpreadPackCompilerIntegrationTest`
- Gradle smoke test `SpreadPackGradleSubpluginSmokeTest`
- 真实 example 工程 `example/example-spread-pack`

还没有单独做一个 Compose runtime smoke test；下面的 Compose `Text` 案例目前是“推荐写法示例”，不是额外的 UI 自动化样例。

## Compose Text 二次封装案例

下面给两个层次的例子：

- 第一层：用 `@SpreadPack` 把一个本地 `Text` wrapper 的 carrier 展开成普通参数
- 第二层：再用 `@SpreadArgsOf` 复用上一层 wrapper 的整体参数表，并排除你不想继续向上暴露的字段

### 1. 直接展开 `Text` wrapper

```kotlin
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack

data class BaseTextArgs(
    val text: String,
    val modifier: Modifier,
    val color: Color,
    val fontSize: TextUnit,
    val fontWeight: FontWeight?,
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
        modifier = args.modifier,
        color = args.color,
        fontSize = args.fontSize,
        fontWeight = args.fontWeight,
        maxLines = args.maxLines,
        overflow = args.overflow,
    )
}
```

然后业务代码可以直接写：

```kotlin
BaseText(
    text = "Hello",
    modifier = Modifier,
    color = Color.Red,
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    maxLines = 1,
)
```

### 2. 再包一层，整体复用参数并排除字段

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadArgsOf
import site.addzero.kcp.spreadpack.SpreadOverload
import site.addzero.kcp.spreadpack.SpreadOverloadsOf
import site.addzero.kcp.spreadpack.SpreadPack

data class TitleTextArgs(
    val text: String,
    val modifier: Modifier,
    val color: Color,
    val fontSize: TextUnit,
    val fontWeight: FontWeight?,
    val maxLines: Int,
)

@Composable
@GenerateSpreadPackOverloads
fun TitleText(
    @SpreadPack
    @SpreadArgsOf(
        overload = SpreadOverload(
            of = SpreadOverloadsOf("site.addzero.demo.BaseText"),
            parameterTypes = [BaseTextArgs::class],
        ),
        exclude = ["overflow"],
    )
    args: TitleTextArgs,
) {
    BaseText(
        text = args.text,
        modifier = args.modifier,
        color = args.color,
        fontSize = args.fontSize,
        fontWeight = args.fontWeight,
        maxLines = args.maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
```

这一层的意思不是“再手写一遍 `BaseText` 参数”，而是：

1. 先选中 `BaseText(BaseTextArgs)`
2. 再把它的展开字段整体拉平
3. 最后排除 `overflow`
4. `TitleText` 自己只保留一套更收敛的上层 API

如果你要包的是原生 Compose `Text`，更实用的做法通常不是直接对 `androidx.compose.material3.Text` 做 `argsof`，而是先定义一个本地 `BaseText` wrapper，再把上层设计系统组件都复用这层 wrapper 的参数表。这样 overload 选择、字段裁剪和演进成本都更可控。

## 生成规则

- carrier 必须是带主构造的普通类
- 当前按主构造参数展开，不扫描任意属性
- `@SpreadPack` 仍然是 carrier 标记
- 当同一个参数同时使用 `@SpreadArgsOf` 时，`@SpreadPack` 必须保持默认配置
- `exclude` 只允许排除带默认值的主构造参数
- `selector = ATTRS` 只展开非函数类型参数
- `selector = CALLBACKS` 只展开函数类型参数
- `@SpreadArgsOf.exclude` 和 `@SpreadArgsOf.selector` 作用在“拉平后的字段集”上
- nested `argsof` 已支持；如果被引用 overload 自己也带 `@SpreadArgsOf`，会继续递归拉平
- 会检测 nested `argsof` 循环；出现 `A -> B -> A` 直接报错
- 如果 overload set 有多个候选，而 `SpreadOverload.parameterTypes` 没给出足够信息，会报歧义错误
- `SpreadOverload.parameterTypes` 当前按参数擦除类型选 overload，不做泛型实参级别匹配
- 被 `argsof` 引用的目标函数当前不能带 receiver 或 context parameters

## 命名与 JVM 落点

- 默认优先生成同名 overload
- 如果 JVM 擦除后与现有签名冲突，会按当前规则改名成 `fooVia<Carrier>Pack`
- 多个 spread-pack 参数会继续按 `And` 连接 suffix
- 顶层生成 overload 的 JVM 落点是 `__GENERATED__CALLABLES__Kt`
- 生成方法会带内部用的 `@GeneratedSpreadPackOverload(sourceFunctionFqName = "...")` 字节码元数据，便于后续 IDE / tooling 识别

## IDE 配套插件

`kcp-spread-pack-ide-plugin` 是这个原型的 JetBrains IDE companion plugin。它当前不把 compiler plugin 直接塞进 IDE 进程，而是走 K2 `KaResolveExtension` 动态提供 stub 文件：

- 让 IDE 能看见由 `@GenerateSpreadPackOverloads` 派生出来的 overload
- 对齐当前 `selector / exclude / nested argsof / member overload set` 语义
- 保持轻量，不把编译器插件 classloader 问题重新带进 IDE

当前它仍然是原型实现，重点是“IDE 先能解析这些派生声明”，不是做完整重构、导航、文档和 quick fix 套餐。

## 当前边界

下面这些能力暂时没有做：

- 裸 `argsof F` 直接语法
- 泛型 carrier
- receiver / context parameter 目标函数的 `argsof`
- 以 IDE 插件方式复刻完整编译器诊断体系
- 基于字节码或 FIR 快照的跨模块全量增量缓存
- 当前原型里，某些“生成 overload + Kotlin 默认参数”组合仍可能触发 Kotlin backend 的 `$default` 代码生成问题；仓库里的 example 已刻意避开这条路径

## 本仓库验证命令

编译器插件：

```bash
./gradlew --configure-on-demand :lib:kcp:spread-pack:kcp-spread-pack-plugin:test --stacktrace
./gradlew --configure-on-demand :lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin:test --stacktrace
```

IDE 插件：

```bash
./gradlew --configure-on-demand :lib:kcp:spread-pack:kcp-spread-pack-ide-plugin:buildPlugin --stacktrace
```
