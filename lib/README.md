# lib 总览

这里是 `addzero-lib-jvm` 的库集合入口，重点不是把所有模块一次讲完，而是先帮你判断该从哪一组开始看。

## 怎么看这个目录

- 想找通用 JVM 工具，先看 `tool-jvm/`
- 想找多平台基础库，先看 `tool-kmp/`
- 想找编译期处理器，先看 `apt/`、`ksp/`、`kcp/`
- 想找 Gradle 侧能力，先看 `gradle-plugin/`
- 想找 Spring Boot Starter，先看 `tool-starter/`

## 一级分组导航

| 目录 | 主要用途 | 可见性倾向 |
| --- | --- | --- |
| [`apt`](./apt/) | APT 时代的注解处理器与代码生成 | 公开优先 |
| [`compose`](./compose/) | Compose 相关组件与试验模块 | 混合 |
| [`decompile`](./decompile/) | 反编译、逆向整理、兼容性试验 | 内部优先 |
| [`gradle-plugin`](./gradle-plugin/) | Gradle Settings / Project / Convention 插件 | 公开优先 |
| [`kcp`](./kcp/) | Kotlin Compiler Plugin 方向实验与实现 | 混合 |
| [`kotlin-script`](./kotlin-script/) | Kotlin Script 相关试验模块 | 混合 |
| [`ksp`](./ksp/) | KSP 处理器、元数据、路由、代码生成 | 混合 |
| [`lsi`](./lsi/) | 语言结构抽象层与多实现适配 | 混合 |
| [`tool-jvm`](./tool-jvm/) | JVM 工具库主集合 | 混合 |
| [`tool-kmp`](./tool-kmp/) | Kotlin Multiplatform 工具库集合 | 混合 |
| [`tool-starter`](./tool-starter/) | Spring Boot Starter 与自动配置 | 公开优先 |

## 选型建议

- 想先找“能直接拿来用”的模块，优先看 `tool-jvm/`、`tool-kmp/`、`tool-starter/`
- 想先找“生成代码/补齐样板”的模块，优先看 `apt/`、`ksp/`、`kcp/`
- 想先找“构建系统和项目组织”相关能力，优先看 `gradle-plugin/`
- `tool-jvm/network-call/` 这一块包含不少第三方接口接入、实验封装和私有模块，默认不进入小鳄鱼文档站
