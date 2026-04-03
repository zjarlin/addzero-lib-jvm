# KCP 相关模块

这一组是 Kotlin Compiler Plugin 方向的实现与实验集合，偏编译期能力。

## 推荐入口

- [`kcp-i18n`](./kcp-i18n/)：国际化方向插件
- [`multireceiver`](./multireceiver/)：多接收者方向实验
- [`singleton-adapter-kcp`](./singleton-adapter-kcp/)：单例适配方向插件
- [`spread-pack`](./spread-pack/)：dataarg / argsof 方向的参数包展开原型
- [`transform-overload`](./transform-overload/)：重载转换方向实验

## 怎么理解这组目录

- 这里很多模块是“一个能力拆成注解、插件、Gradle 接入、样例”的组合
- 看某个能力时，优先先看顶层 README，再回到子模块实现

## 备注

- 这组能力通常更适合已经在做编译期扩展的人
- 如果你只是在找业务工具库，通常先看 `tool-jvm/` 或 `tool-kmp/`
