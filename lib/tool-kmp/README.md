# tool-kmp 总览

这里是 Kotlin Multiplatform 方向的工具库集合，覆盖基础类型、集合、字符串、Ktor、JDBC 和一些启动器能力。

## 推荐入口

- [`tool-regex`](./tool-regex/)：正则表达式工具
- `jdbc/`：多平台 JDBC 相关能力
- `ktor/`：Ktor 相关模块与 starter
- `models/`：公共模型层
- `network-starter/`：网络启动器方向

## 怎么理解这组目录

- `tool-*`：按能力拆分的基础库
- `ktor/`：Web 与服务端基础设施
- `jdbc/`：数据库访问模型
- `models/`：可复用模型定义

## 适合什么时候看

- 你要做 KMP 基础能力复用
- 你想找不依赖纯 JVM 的公共模块
