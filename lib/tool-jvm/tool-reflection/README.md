# Tool Reflection

这是一个改进的反射工具库，解决了 Hutool ReflectUtil 中存在的内存泄漏问题。

## 特性

1. **内存安全** - 使用带过期时间和大小限制的缓存机制
2. **高性能** - 保留缓存优势的同时避免内存泄漏
3. **易于使用** - API 设计与 Hutool ReflectUtil 类似，迁移成本低
4. **主动清理** - 提供手动清理缓存的方法

## 使用方法

### 基本用法

```kotlin
// 获取构造函数
val constructor = ImprovedReflectUtil.getConstructor(MyClass::class.java, String::class.java)
val instance = constructor?.newInstance("test")

// 获取字段
val fields = ImprovedReflectUtil.getFields(MyClass::class.java)

// 获取方法
val methods = ImprovedReflectUtil.getMethods(MyClass::class.java)
```

### 缓存管理

```kotlin
// 清理所有缓存
ImprovedReflectUtil.clearAllCaches()

// 清理过期条目
ImprovedReflectUtil.cleanupExpiredEntries()
```

## 解决的问题

Hutool 的 ReflectUtil 使用 WeakConcurrentMap 作为缓存，但仍然可能出现内存泄漏问题：

1. **弱引用限制** - 弱引用只能在下次GC时清理，不能及时释放内存
2. **缓存无限增长** - 在高并发或大量不同类的场景下，缓存可能持续增长
3. **缺乏主动清理机制** - 没有提供清理缓存的方法

本工具类通过以下方式解决了这些问题：

1. **带过期时间的缓存** - 默认30分钟过期时间
2. **最大容量限制** - 默认最大1000个条目
3. **主动清理机制** - 提供手动清理缓存的方法
4. **智能淘汰策略** - 当达到容量上限时，自动移除最旧的一半条目

## 迁移指南

如果你正在使用 Hutool 的 ReflectUtil，可以通过以下步骤迁移到本工具类：

1. 将 `cn.hutool.core.util.ReflectUtil` 替换为 `site.addzero.util.ImprovedReflectUtil`
2. 在适当的地方调用 `clearAllCaches()` 或 `cleanupExpiredEntries()` 来管理内存

示例：

```java
// 旧代码
// import cn.hutool.core.util.ReflectUtil;
// ReflectUtil.getMethods(MyClass.class);

// 新代码
import site.addzero.util.ImprovedReflectUtil;
ImprovedReflectUtil.getMethods(MyClass.class);
```