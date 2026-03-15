# tool-jvm 总览

这里是 JVM 工具库主集合，通用能力、数据库相关、网络调用封装和一批 `tool-*` 模块基本都在这里。

## 推荐入口

- [`database/ddlgenerator`](./database/ddlgenerator/)：数据库 DDL 生成方向
- [`netty-util`](./netty-util/)：Netty 相关工具
- [`tool-handwriting`](./tool-handwriting/)：手写识别相关能力
- [`tool-reflection`](./tool-reflection/)：反射工具
- [`tool-s3`](./tool-s3/)：对象存储 / S3 相关封装

## 怎么理解这组目录

- `database/`：数据库、DDL、SQL 相关能力
- `jimmer/`：Jimmer 扩展
- `network-call/`：第三方接口调用与远程封装，默认按内部优先处理
- 其余大量 `tool-*`：按能力拆分的 JVM 通用工具

## 选型建议

- 找通用基础能力，优先从 `tool-common-jvm`、`tool-api-jvm`、`tool-context` 这类名字开始排查
- 找具体领域能力，就先看对应子目录名，比如 `database/`、`jimmer/`、`tool-email`
- `network-call/` 下不少模块不会进入小鳄鱼文档站，但源码目录里仍然保留 README 导航
