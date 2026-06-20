# tool-date

日期时间转换与工作日工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-date")
```

主要能力：

- `Conversions.toDate(...)` / `Conversions.toLocalDate(...)`：在 Java Date 与 Java Time 类型之间转换。
- `Conversions.getWeek(...)`：获取中文星期。
- `Conversions.countWorkDay(...)`：按周末规则统计工作日。
