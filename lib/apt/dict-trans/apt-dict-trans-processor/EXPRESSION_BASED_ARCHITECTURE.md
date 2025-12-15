# 基于表达式的翻译架构设计

## 核心理念

编译时生成表达式，运行时通过单例工厂和咖啡因缓存处理，实现最佳性能和缓存效果。

## 架构组件

### 1. TransTask - 翻译任务（包含表达式）
```kotlin
data class TransTask(
    val taskId: String,                    // "gender_task"
    val fieldPath: String,                 // "gender"
    val valueExpression: String,           // "dto.getGender()"
    val dictType: String,                  // "system"
    val dictConfig: String,                // "sys_user_sex"
    val setterExpression: String,          // "dto.setGenderName(translatedValue)"
    val nestedClassPrefix: String = "",    // ""
    val priority: Int = 0
)
```

### 2. DictTranslationFactory - 单例工厂（咖啡因缓存）
```kotlin
object DictTranslationFactory {
    // 系统字典缓存: "sys_user_sex:0" -> "男"
    private val systemDictCache: Cache<String, String>
    
    // 表字典缓存: "equipment:id:name:49" -> "设备A"
    private val tableDictCache: Cache<String, String>
    
    // 预编译SQL缓存
    private val precompiledSqlCache: Cache<String, PrecompiledSql>
    
    fun processTranslationTasks(
        tasks: List<TransTask>,
        valueExtractor: (String) -> Any?
    ): CompletableFuture<Map<String, String>>
}
```

## 生成的代码示例

### ComplexNestedEntityConvertor.java

```java
public class ComplexNestedEntityConvertor implements LsiDictConvertor<ComplexNestedEntity, ComplexNestedEntityDictDTO> {
    
    public ComplexNestedEntityConvertor(SqlExecutor sqlExecutor) {
        // Initialize the singleton factory
        DictTranslationFactory.initialize(sqlExecutor);
    }
    
    @Override
    public ComplexNestedEntityDictDTO code2name(ComplexNestedEntity entity) {
        return code2name(Collections.singletonList(entity)).get(0);
    }
    
    public List<ComplexNestedEntityDictDTO> code2name(List<ComplexNestedEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Convert to DTOs
        List<ComplexNestedEntityDictDTO> dtos = entities.stream()
            .filter(Objects::nonNull)
            .map(ComplexNestedEntityDictDTO::new)
            .collect(Collectors.toList());
        
        // Get compile-time generated translation tasks
        List<TransTask> tasks = getTranslationTasks();
        
        // Execute batch translation using singleton factory
        try {
            CompletableFuture<Map<String, String>> future = 
                DictTranslationFactory.processTranslationTasks(tasks, this::extractValue);
            Map<String, String> translationResults = future.get();
            
            // Apply translations using generated code
            applyTranslationResults(dtos, translationResults);
            
        } catch (Exception e) {
            throw new RuntimeException("Batch translation failed", e);
        }
        
        return dtos;
    }
    
    /**
     * Get compile-time generated translation tasks
     */
    private List<TransTask> getTranslationTasks() {
        List<TransTask> tasks = new ArrayList<>();
        
        // Root level fields
        tasks.add(new TransTask(
            "gender_task",
            "gender",
            "dto.getGender()",
            "system",
            "sys_user_sex",
            "dto.setGenderName(translatedValue)",
            "",
            0
        ));
        
        tasks.add(new TransTask(
            "deviceStatus_task",
            "deviceStatus",
            "dto.getDeviceStatus()",
            "system",
            "sys_show_hide",
            "dto.setDeviceStatusName(translatedValue)",
            "",
            0
        ));
        
        tasks.add(new TransTask(
            "productKey_task",
            "productKey",
            "dto.getProductKey()",
            "table",
            "iot_product|product_key|product_name|",
            "dto.setProductKeyName(translatedValue)",
            "",
            0
        ));
        
        // Nested fields
        tasks.add(new TransTask(
            "deviceId_task",
            "deviceInfo.deviceId",
            "dto.getDeviceInfo() != null ? dto.getDeviceInfo().getDeviceId() : null",
            "table",
            "equipment|id|code|",
            "if (dto.getDeviceInfo() != null) { dto.getDeviceInfo().setDeviceIdName(translatedValue); }",
            "ComplexNestedEntity.DeviceInfo",
            0
        ));
        
        // Deep nested fields
        tasks.add(new TransTask(
            "testvar1_task",
            "deviceInfo.location.testvar1",
            "dto.getDeviceInfo() != null && dto.getDeviceInfo().getLocation() != null ? dto.getDeviceInfo().getLocation().getTestvar1() : null",
            "system",
            "sys_normal_disable",
            "if (dto.getDeviceInfo() != null && dto.getDeviceInfo().getLocation() != null) { dto.getDeviceInfo().getLocation().setTestvar1Name(translatedValue); }",
            "ComplexNestedEntity.DeviceInfo.Location",
            0
        ));
        
        return tasks;
    }
    
    /**
     * Extract value using expression (used by singleton factory)
     */
    private Object extractValue(String expression) {
        // This method will be generated with specific extraction logic
        if ("dto.getGender()".equals(expression)) {
            return dto.getGender();
        }
        if ("dto.getDeviceStatus()".equals(expression)) {
            return dto.getDeviceStatus();
        }
        if ("dto.getProductKey()".equals(expression)) {
            return dto.getProductKey();
        }
        if ("dto.getDeviceInfo() != null ? dto.getDeviceInfo().getDeviceId() : null".equals(expression)) {
            return dto.getDeviceInfo() != null ? dto.getDeviceInfo().getDeviceId() : null;
        }
        if ("dto.getDeviceInfo() != null && dto.getDeviceInfo().getLocation() != null ? dto.getDeviceInfo().getLocation().getTestvar1() : null".equals(expression)) {
            return dto.getDeviceInfo() != null && dto.getDeviceInfo().getLocation() != null ? dto.getDeviceInfo().getLocation().getTestvar1() : null;
        }
        return null; // Default fallback
    }
    
    /**
     * Apply translation results using task IDs
     */
    private void applyTranslationResults(
        List<ComplexNestedEntityDictDTO> dtos, 
        Map<String, String> translationResults
    ) {
        // Apply translation for task: gender_task
        String genderResult = translationResults.get("gender_task");
        if (genderResult != null) {
            for (ComplexNestedEntityDictDTO dto : dtos) {
                String translatedValue = genderResult;
                dto.setGenderName(translatedValue);
            }
        }
        
        // Apply translation for task: deviceStatus_task
        String deviceStatusResult = translationResults.get("deviceStatus_task");
        if (deviceStatusResult != null) {
            for (ComplexNestedEntityDictDTO dto : dtos) {
                String translatedValue = deviceStatusResult;
                dto.setDeviceStatusName(translatedValue);
            }
        }
        
        // Apply translation for task: productKey_task
        String productKeyResult = translationResults.get("productKey_task");
        if (productKeyResult != null) {
            for (ComplexNestedEntityDictDTO dto : dtos) {
                String translatedValue = productKeyResult;
                dto.setProductKeyName(translatedValue);
            }
        }
        
        // Apply translation for task: deviceId_task
        String deviceIdResult = translationResults.get("deviceId_task");
        if (deviceIdResult != null) {
            for (ComplexNestedEntityDictDTO dto : dtos) {
                String translatedValue = deviceIdResult;
                if (dto.getDeviceInfo() != null) { dto.getDeviceInfo().setDeviceIdName(translatedValue); }
            }
        }
        
        // Apply translation for task: testvar1_task
        String testvar1Result = translationResults.get("testvar1_task");
        if (testvar1Result != null) {
            for (ComplexNestedEntityDictDTO dto : dtos) {
                String translatedValue = testvar1Result;
                if (dto.getDeviceInfo() != null && dto.getDeviceInfo().getLocation() != null) { dto.getDeviceInfo().getLocation().setTestvar1Name(translatedValue); }
            }
        }
    }
}
```

## 工作流程

### 1. 编译时
1. 扫描 `@Dict` 注解
2. 生成 `TransTask` 任务列表，包含：
   - 值提取表达式：`dto.getGender()`
   - 值设置表达式：`dto.setGenderName(translatedValue)`
3. 生成值提取器方法
4. 生成结果应用方法

### 2. 运行时
1. 转换实体为DTO列表
2. 调用单例工厂处理翻译任务
3. 工厂使用表达式提取值
4. 查询缓存，未命中则批量查询数据库
5. 更新缓存并返回翻译结果
6. 应用翻译结果到DTO

## 缓存策略

### 系统字典缓存
```
Key: "sys_user_sex:0"
Value: "男"
TTL: 30分钟
Max Size: 10,000
```

### 表字典缓存
```
Key: "equipment:id:name:49"
Value: "设备A"
TTL: 15分钟
Max Size: 50,000
```

### 预编译SQL缓存
```
Key: "system:sys_user_sex"
Value: PrecompiledSql对象
TTL: 1小时
Max Size: 1,000
```

## 性能优势

### 1. 缓存命中率优化
- **智能分层缓存**: 系统字典和表字典分别缓存
- **批量查询防重**: 防止同一时间的重复查询
- **TTL差异化**: 根据数据特性设置不同的过期时间

### 2. 表达式执行优化
- **编译时生成**: 所有表达式在编译时确定
- **直接方法调用**: 无反射开销
- **空值安全**: 自动生成空值检查

### 3. 内存优化
- **单例模式**: 全局共享缓存和连接池
- **自动清理**: 基于LRU和TTL的自动清理
- **统计监控**: 提供详细的缓存统计信息

## 监控和调试

### 缓存统计
```kotlin
val stats = DictTranslationFactory.getCacheStats()
println("System dict cache hit rate: ${stats.systemDictCacheStats.hitRate()}")
println("Table dict cache size: ${stats.tableDictCacheSize}")
```

### 性能监控
- 缓存命中率
- 查询响应时间
- 内存使用情况
- 并发查询数量

这种架构结合了编译时优化和运行时缓存的优势，实现了最佳的性能和可维护性。