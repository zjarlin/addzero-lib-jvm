# tool-obj

对象与运行时值判断工具。

Maven 坐标：

```kotlin
implementation("site.addzero:tool-obj")
```

主要能力：

- `JlObjUtil.isDate(value)`：判断值是否可作为日期处理。
- `Iterator<*>?.isEmpty()`：兼容 Hutool 的迭代器空判断。
