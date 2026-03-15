# KSP 相关模块

这一组是仓库里编译期代码生成最活跃的一块，既有公共支撑，也有面向具体场景的处理器。

## 推荐入口

- [`common/ksp-easycode`](./common/ksp-easycode/)：通用 KSP 模板与基础能力
- [`metadata/controller2feign-processor`](./metadata/controller2feign-processor/)：Controller 到 Feign 生成
- [`metadata/entity2iso-processor`](./metadata/entity2iso-processor/)：实体同构体生成
- [`metadata/ioc/ioc-processor`](./metadata/ioc/ioc-processor/)：IOC 处理器
- [`metadata/method-semanticizer`](./metadata/method-semanticizer/)：方法语义化方向
- [`metadata/singleton-adapter-processor`](./metadata/singleton-adapter-processor/)：单例适配处理器
- [`metadata/spring2ktor-server-processor`](./metadata/spring2ktor-server-processor/)：Spring 到 Ktor 迁移辅助

## 分组理解

- `common/`：公共底座
- `metadata/`：面向具体元数据和生成目标的核心处理器
- `route/`：路由相关
- `jdbc2metadata/`：从数据库或 JDBC 信息出发的生成方向

## 适合什么时候看

- 想知道这个仓库的代码生成主力都放在哪
- 想找可复用的 KSP 处理器结构和拆分方式
