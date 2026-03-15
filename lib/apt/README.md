# APT 相关模块

这一组主要放 Java Annotation Processing 时代的处理器和配套代码生成能力。

## 推荐入口

- [`apt-dict-processor`](./apt-dict-processor/)：字典枚举相关生成器
- [`apt-ioc-processor`](./apt-ioc-processor/)：IOC 相关处理器
- [`apt-controller2feign-processor`](./apt-controller2feign-processor/)：Controller 到 Feign 的生成方向
- [`dict-trans/apt-dict-trans-processor`](./dict-trans/apt-dict-trans-processor/)：字典翻译处理器

## 这一组适合什么时候看

- 项目还在 APT 体系，不准备整体迁移到 KSP
- 需要兼容老模块、老注解、老构建链路
- 想先理解这套生成逻辑的历史版本，再决定是否迁移

## 备注

- `dict-trans/` 是一个小分支目录，不只放单一处理器
- 如果你是新项目，通常也值得顺手对比一下 `../ksp/`
