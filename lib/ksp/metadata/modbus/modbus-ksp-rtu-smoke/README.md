# modbus-ksp-rtu-smoke

`modbus-ksp-rtu-smoke` 是仓库内部用来验证 RTU 整条生成链是否还正常的冒烟测试模块。

- 本地路径：`lib/ksp/metadata/modbus/modbus-ksp-rtu-smoke`

## 它不是给谁用的

这个模块不是：

- 发布给业务工程的 API 模块
- 推荐消费入口
- 通用样板工程

它的角色只有一个：

- 帮仓库自己锁定 “真实注解契约 -> KSP -> Kotlin/C/Markdown -> 外部工程同步” 这一整条链路没有被改坏

## 它覆盖了什么

这个模块会真实运行 `modbus-ksp-rtu`，验证：

- Kotlin gateway 生成
- C contract / dispatch / bridge 生成
- Markdown 文档生成
- Spring 风格源码输出
- 外部固件工程镜像输出
- `.uvprojx` / `.mxproject` 同步
- 地址锁文件写入

## 怎么运行

```bash
./gradlew :lib:ksp:metadata:modbus:modbus-ksp-rtu-smoke:test
```

## 如果你要找可抄的样例

这个模块不是最终业务模板，但它很适合拿来对照两个文件：

- `src/main/kotlin/site/addzero/device/contract/DeviceApi.kt`
- `src/main/kotlin/site/addzero/device/contract/FlashApi.kt`

这两个文件展示了当前 RTU 生成链接受的真实输入长什么样。
