# 懒加载翻译架构设计

## 核心思想

通过 `TransTaskInfo` 上下文收集翻译任务，使用懒加载机制避免N+1查询问题。

## 架构组件

### 1. TransTaskInfo - 翻译任务信息
```kotlin
data class TransTaskInfo(
    val taskId: String,           // 唯一任务ID
    val fieldName: String,        // 字段名
    val originalValue: Any?,      // 原始值
    val dictType: String,         // 字典类型：system/table
    val dictConfig: String,       // 字典配置
    val targetObject: Any,        // 目标对象
    val targetFieldName: String,  // 目标字段名
    val priority: Int = 0         // 优先级
)
```

### 2. TransTaskContext - 任务上下文
- 线程安全的任务收集器
- 支持任务去重和分组
- 提供结果缓存机制

### 3. BatchTranslationExecutor - 批量执行器
- 消费任务上下文中的任务
- 生成预编译SQL
- 执行批量查询
- 应用翻译结果

## 工作流程

### 1. 任务收集阶段（Lazy Emit）
```java
// 为每个需要翻译的字段生成任务
Object userTypeValue = dto.getUserType();
if (userTypeValue != null) {
    String taskId = "userType_" + i + "_" + userTypeValue.toString();
    TransTaskInfo task = new TransTaskInfo(
        taskId,
        "userType",
        userTypeValue,
        "system",
        "user_type",
        dto,
        "userTypeName",
        0
    );
    context.lazyEmit(task);
}
```

### 2. 批量执行阶段
```java
// 创建翻译上下文
TransTaskContext context = new TransTaskContext();

// 懒加载发射翻译任务
lazyEmitTranslationTasks(dtos, context);

// 执行批量翻译
CompletableFuture<Void> future = batchExecutor.executeBatchTranslation(context);
future.get(); // 等待完成
```

### 3. SQL生成和执行
```sql
-- 系统字典批量查询
SELECT dict_code as code, dict_name as name 
FROM sys_dict_data 
WHERE dict_type = ? AND dict_code IN (?,?,?)
AND status = '0' AND del_flag = '0'

-- 表字典批量查询
SELECT code_column as code, name_column as name 
FROM custom_table 
WHERE code_column IN (?,?,?)
```

## 优势

### 1. 性能优化
- **消除N+1问题**: 将多次单独查询合并为批量查询
- **预编译SQL**: 提高查询执行效率
- **结果缓存**: 避免重复查询相同数据

### 2. 内存优化
- **懒加载**: 只在需要时收集和处理任务
- **任务去重**: 避免重复处理相同的翻译任务
- **及时清理**: 处理完成后清理上下文

### 3. 扩展性
- **异步执行**: 支持异步批量处理
- **优先级支持**: 可以按优先级处理任务
- **插件化**: 易于扩展新的字典类型

## 使用示例

### 生成的Convertor类
```java
public class UserConvertor implements LsiDictConvertor<User, UserDictDTO> {
    
    private final BatchTranslationExecutor batchExecutor;
    
    public List<UserDictDTO> code2name(List<User> entities) {
        // 转换为DTO
        List<UserDictDTO> dtos = entities.stream()
            .map(UserDictDTO::new)
            .collect(Collectors.toList());
        
        // 创建翻译上下文
        TransTaskContext context = new TransTaskContext();
        
        // 懒加载发射翻译任务
        lazyEmitTranslationTasks(dtos, context);
        
        // 执行批量翻译
        batchExecutor.executeBatchTranslation(context).get();
        
        return dtos;
    }
}
```

### 性能对比

#### 传统方式（N+1问题）
```
查询1: SELECT * FROM users WHERE id IN (1,2,3,4,5)
查询2: SELECT dict_name FROM sys_dict_data WHERE dict_code = 'type1'
查询3: SELECT dict_name FROM sys_dict_data WHERE dict_code = 'type2'
查询4: SELECT dict_name FROM sys_dict_data WHERE dict_code = 'type1'
查询5: SELECT dict_name FROM sys_dict_data WHERE dict_code = 'type3'
查询6: SELECT dict_name FROM sys_dict_data WHERE dict_code = 'type2'
总计: 6次查询
```

#### 懒加载方式
```
查询1: SELECT * FROM users WHERE id IN (1,2,3,4,5)
查询2: SELECT dict_code, dict_name FROM sys_dict_data WHERE dict_type = 'user_type' AND dict_code IN ('type1','type2','type3')
总计: 2次查询
```

## 配置和扩展

### 自定义执行器
```java
// 使用自定义线程池
Executor customExecutor = Executors.newFixedThreadPool(4);
BatchTranslationExecutor executor = new BatchTranslationExecutor(sqlExecutor, customExecutor);
```

### 监控和统计
```java
TransTaskContext context = new TransTaskContext();
// ... 处理任务 ...
TaskStats stats = context.getStats();
System.out.println("Total tasks: " + stats.getTotalTasks());
System.out.println("System dict tasks: " + stats.getSystemDictTasks());
System.out.println("Table dict tasks: " + stats.getTableDictTasks());
```

这种架构设计彻底解决了字典翻译的N+1问题，同时提供了良好的性能和扩展性。