# kbox-ssh

`kbox-ssh` 提供 `KBox` 的 SSH/SFTP 远程传输实现，并以 Koin 模块方式暴露给宿主。

- Maven 坐标：`site.addzero:kbox-ssh`
- 本地模块：`/Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/kbox-ssh`

最小用法：

```kotlin
@KoinApplication(
    modules = [
        KboxCoreKoinModule::class,
        KboxSshKoinModule::class,
    ],
)
object MyKboxRuntime
```

运行约束：

- 当前模块仅支持 JVM。
- 传输默认使用 `SSHJ` 的 SFTP 实现。
