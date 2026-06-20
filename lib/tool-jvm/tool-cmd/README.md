# tool-cmd

本地命令执行工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-cmd")
```

主要能力：

- `CmdUtil.runCmd(cmd)`：按当前系统选择 PowerShell 或 Bash 执行命令。
- `site.addzero.common.util.shell.CommandExecutor`：保留旧 shell 执行入口。
- `site.addzero.common.util.bash.CommandExecutor`：保留旧 Bash 脚本执行入口。
