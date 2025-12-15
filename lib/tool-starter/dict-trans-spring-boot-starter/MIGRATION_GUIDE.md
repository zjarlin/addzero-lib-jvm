# ByteBuddyUtil 迁移指南

## 概述

`ByteBuddyUtil` 已被标记为弃用，推荐使用 `OptimizedByteBuddyUtil` 替代，以获得更好的性能和批量处理能力。

## 迁移步骤

### 1. 替换单个对象处理

**旧代码：**
```kotlin
val result = ByteBuddyUtil.genChildObjectRecursion(obj) { o ->
    TransInternalUtil.getNeedAddFields(o).toMutableList()
}
```

**新代码：**
```kotlin
val results = OptimizedByteBuddyUtil.genChildObjectsBatch(listOf(obj)) { o ->
    TransInternalUtil.getNeedAddFields(o).toMutableList()
}
val result = results.firstOrNull()
```

### 2. 替换批量对象处理

**旧代码：**
```kotlin
val collect = inVOs.map { e ->
    val o = ByteBuddyUtil.genChildObjectRecursion(e) { obj ->
        val needAddFields = TransInternalUtil.getNeedAddFields(obj)
        needAddFields.toMutableList()
    }
    o
}
```

**新代码：**
```kotlin
val collect = OptimizedByteBuddyUtil.genChildObjectsBatch(inVOs) { obj ->
    TransInternalUtil.getNeedAddFields(obj).toMutableList()
}
```

### 3. 处理Java Function接口

如果你的代码中使用了Java的Function接口：

**旧代码：**
```kotlin
ByteBuddyUtil.genChildObjectRecursion(obj, Function { o ->
    TransInternalUtil.getNeedAddFields(o).toMutableList()
})
```

**新代码：**
```kotlin
OptimizedByteBuddyUtil.genChildObjectsBatch(listOf(obj)) { o ->
    TransInternalUtil.getNeedAddFields(o).toMutableList()
}
```

## 性能优势

### 1. 减少字节码生成次数
- **旧方式**：每个对象实例都生成新的字节码类
- **新方式**：每个类型只生成一次字节码类，后续复用

### 2. 批量处理优化
- **旧方式**：逐个处理对象，重复分析嵌套结构
- **新方式**：一次性分析所有对象的嵌套结构，收集字段需求并集

### 3. 内存使用优化
- **旧方式**：相同类型的对象生成多个字节码类
- **新方式**：缓存生成的字节码类，减少内存占用

## 兼容性说明

### 1. 返回值类型
- 两种方式的返回值类型完全一致
- 新方式返回List，单个对象处理时取第一个元素即可

### 2. 嵌套对象处理
- 两种方式都正确处理嵌套对象和集合
- 新方式在处理复杂嵌套结构时性能更优

### 3. 字段需求函数
- 函数签名完全一致：`(Any) -> MutableList<NeedAddInfo>`
- 无需修改现有的字段需求分析逻辑

## 注意事项

### 1. 缓存管理
新方式使用缓存来提升性能，在某些场景下可能需要手动清理：

```kotlin
// 清理缓存（通常不需要）
OptimizedByteBuddyUtil.clearCache()

// 查看缓存统计
val stats = OptimizedByteBuddyUtil.getCacheStats()
println("缓存统计: $stats")
```

### 2. 线程安全
新的实现是线程安全的，可以在多线程环境中使用。

### 3. 错误处理
新实现提供了更详细的错误信息，便于调试：

```kotlin
try {
    val results = OptimizedByteBuddyUtil.genChildObjectsBatch(objects) { obj ->
        TransInternalUtil.getNeedAddFields(obj).toMutableList()
    }
} catch (e: RuntimeException) {
    // 错误信息包含类名、字段需求等详细信息
    logger.error("字节码生成失败: ${e.message}", e)
}
```

## 迁移检查清单

- [ ] 替换所有 `ByteBuddyUtil.genChildObjectRecursion` 调用
- [ ] 将单个对象处理改为批量处理
- [ ] 移除Java Function接口的使用
- [ ] 测试性能提升效果
- [ ] 验证功能正确性
- [ ] 更新相关文档和注释

## 回滚方案

如果迁移过程中遇到问题，可以暂时保留旧代码：

```kotlin
// 临时回滚到旧实现
@Suppress("DEPRECATION")
val result = ByteBuddyUtil.genChildObjectRecursion(obj) { o ->
    TransInternalUtil.getNeedAddFields(o).toMutableList()
}
```

但建议尽快解决问题并完成迁移，因为旧实现在未来版本中可能被移除。