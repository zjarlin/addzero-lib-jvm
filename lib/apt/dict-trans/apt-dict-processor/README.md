# APT 字典翻译处理器

基于 KSP (Kotlin Symbol Processing) 的编译时字典翻译方案，相比反射实现具有更好的性能和类型安全性。

## 特性

- 🚀 **编译时生成**：零运行时反射，性能更优
- 🔒 **类型安全**：编译时检查，避免运行时错误
- 🎯 **批量优化**：自动生成批量翻译逻辑
- 🔧 **灵活配置**：支持系统字典和自定义表翻译
- 🌟 **Spring 集成**：提供 Spring Boot Starter
- 📦 **扩展友好**：支持扩展函数和 Builder 模式

## 快速开始

### 1. 添加依赖

```kotlin
dependencies {
    // 注解
    implementation("site.addzero:apt-dict-annotations:${version}")
    
    // KSP 处理器
    ksp("site.addzero:apt-dict-processor:${version}")
    
    // Spring Boot Starter（可选）
    implementation("site.addzero:apt-dict-spring-boot-starter:${version}")
}
```

### 2. 定义数据类

```kotlin
@DictTranslate
data class UserVO(
    val id: Long,
    val name: String,
    
    @DictField(dictCode = "user_status", targetField = "statusName")
    val status: String,
    
    @DictField(
        table = "sys_dept", 
        codeColumn = "id", 
        nameColumn = "name", 
        targetField = "deptName"
    )
    val deptId: Long
)
```

### 3. 使用生成的代码

```kotlin
// 原始对象
val user = UserVO(1L, "张三", "1", 100L)

// 转换为增强对象
val enhancedUser = user.toEnhanced()

// 执行字典翻译
enhancedUser.translate(dictService)

// 访问翻译结果
println(enhancedUser.statusName) // "正常"
println(enhancedUser.deptName)   // "技术部"
```

## 注解说明

### @DictTranslate

标记需要进行字典翻译的类。

```kotlin
@DictTranslate(
    suffix = "Enhanced",           // 生成类的后缀，默认 "Enhanced"
    generateExtensions = true,     // 是否生成扩展函数，默认 true
    generateBuilder = false        // 是否生成 Builder 模式，默认 false
)
```

### @DictField

标记需要翻译的字段。

```kotlin
@DictField(
    dictCode = "user_status",      // 系统字典编码
    table = "sys_dept",            // 自定义表名
    codeColumn = "id",             // 编码列名
    nameColumn = "name",           // 名称列名
    targetField = "statusName",    // 目标字段名
    spelExp = "",                  // SpEL 表达式
    ignoreNull = true,             // 是否忽略空值
    defaultValue = "",             // 默认值
    cached = true                  // 是否缓存
)
```

## 配置

### Spring Boot 配置

```yaml
site:
  addzero:
    dict:
      translate:
        enabled: true                    # 是否启用
        dict-table: sys_dict_data       # 字典表名
        enable-cache: true              # 是否启用缓存
        cache-expire-seconds: 300       # 缓存过期时间
        batch-size: 100                 # 批量大小
        enable-async: false             # 是否启用异步
```

## 生成的代码示例

对于上面的 `UserVO`，将生成：

```kotlin
data class UserVOEnhanced(
    val id: Long,
    val name: String,
    val status: String,
    val deptId: Long
) {
    var statusName: String? = null
    var deptName: String? = null
    
    fun translate(dictService: DictService) {
        this.statusName = dictService.translateByDictCode("user_status", this.status)
        this.deptName = dictService.translateByTable("sys_dept", "id", "name", this.deptId)
    }
}

fun UserVO.toEnhanced(): UserVOEnhanced {
    return UserVOEnhanced(id, name, status, deptId)
}
```

## 性能对比

| 方案 | 初始化时间 | 翻译性能 | 内存占用 | 类型安全 |
|------|------------|----------|----------|----------|
| 反射方案 | 慢 | 慢 | 高 | 否 |
| APT 方案 | 快 | 快 | 低 | 是 |

## 最佳实践

1. **批量翻译**：对于列表数据，使用批量翻译接口
2. **缓存策略**：合理配置缓存过期时间
3. **字段命名**：使用有意义的 `targetField` 名称
4. **异常处理**：实现自定义的 `DictService` 处理异常情况

## 扩展

### 自定义字典服务

```kotlin
@Service
class CustomDictService : DictService {
    override fun translateByDictCode(dictCode: String, key: String?): String? {
        // 自定义实现
    }
    
    // ... 其他方法
}
```

### 自定义缓存

```kotlin
@Configuration
class CacheConfig {
    @Bean
    fun dictCacheManager(): CacheManager {
        // 自定义缓存配置
    }
}
```