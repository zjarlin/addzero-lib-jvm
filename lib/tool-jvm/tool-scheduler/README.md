# tool-scheduler

轻量本地定时任务工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-scheduler")
```

主要能力：

- `TaskScheduler.scheduleOnce(...)`：延迟执行一次。
- `TaskScheduler.scheduleAtFixedRate(...)`：固定间隔执行。
- `TaskScheduler.scheduleDaily(...)`：按每天固定时间执行。
