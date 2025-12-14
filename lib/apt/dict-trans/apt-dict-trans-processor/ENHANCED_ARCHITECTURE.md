# 增强的字典翻译架构

## 架构重新设计

根据您的建议，我重新设计了字典翻译的代码生成架构，主要改进包括：

### 1. 核心接口位置修正 ✅

**LsiDictConvertor** 现在正确位于核心包中：
- 位置: `lib/apt/dict-trans/apt-dict-trans-core/src/main/kotlin/site/addzero/dict/trans/inter/LsiDictConvertor.kt`
- 作用: 供生成的代码实现，提供双向转换功能

### 2. DictDTO 独立设计 ✅

**不再继承原实体**，而是独立的DTO类：
```java
@Data
public class UserDictDTO {
    // 原始字段（递归处理嵌套对象）
    private String name;
    private DeptDictDTO dept;           // Dept -> DeptDictDTO
    private List<RoleDictDTO> roles;    // List<Role> -> List<RoleDictDTO>
    
    // 字典翻译字段
    private String statusText;
    private String genderText;
}
```

### 3. 递归嵌套对象处理 ✅

**支持复杂嵌套结构**：
- `Dept dept` → `DeptDictDTO dept`
- `List<Role> roles` → `List<RoleDictDTO> roles`
- 自动识别自定义类型并生成对应的DTO

### 4. IoC上下文管理 ✅

**DictClassHelperIocContext** 管理所有映射关系：
```java
public class DictClassHelperIocContext {
    // 类映射表：原始类 -> DTO类
    private final Map<Class<?>, Class<?>> classToDtoMap;
    
    // 导包映射表：类名 -> 全限定名
    private final Map<String, String> classNameToPackageMap;
    
    // 转换器映射表：原始类 -> 转换器实例
    private final Map<Class<?>, LsiDictConvertor<?, ?>> convertorMap;
    
    // 工具方法
    public <T, D> Class<D> getDtoClass(Class<T> originalClass);
    public <T, D> LsiDictConvertor<T, D> getConvertor(Class<T> originalClass);
}
```

## 生成的代码结构

### 1. DictDTO类
- **独立的数据传输对象**
- **递归处理嵌套对象和集合**
- **包含字典翻译字段**
- **提供双向转换方法**

### 2. Convertor类
- **实现LsiDictConvertor接口**
- **提供code2name和name2code方法**
- **处理复杂的嵌套转换逻辑**

### 3. SqlAssist类
- **SQL辅助工具**
- **生成优化的查询语句**
- **支持批量操作**

### 4. DictDsl类
- **向后兼容的DSL接口**
- **简化的API调用**

### 5. DictClassHelperIocContext
- **全局映射管理器**
- **类型安全的转换器获取**
- **运行时类型映射**

## 使用示例

### 原始实体
```java
public class User {
    private String name;
    private Dept dept;
    private List<Role> roles;
    
    @Dict(dicCode = "user_status")
    private String status;
    
    @Dict(dicCode = "gender")
    private String gender;
}
```

### 生成的DTO
```java
@Data
public class UserDictDTO {
    private String name;
    private DeptDictDTO dept;           // 递归处理
    private List<RoleDictDTO> roles;    // 集合递归处理
    private String status;
    private String gender;
    
    // 字典翻译字段
    private String statusText;
    private String genderText;
    
    // 转换方法
    public static UserDictDTO fromOriginal(User original);
    public User toOriginal();
}
```

### 生成的转换器
```java
public class UserConvertor implements LsiDictConvertor<User, UserDictDTO> {
    @Override
    public UserDictDTO code2name(User entity) {
        UserDictDTO dto = UserDictDTO.fromOriginal(entity);
        // 执行字典翻译
        performDictTranslation(dto);
        return dto;
    }
    
    @Override
    public User name2code(UserDictDTO dto) {
        return dto.toOriginal();
    }
}
```

### IoC上下文使用
```java
// 获取转换器
LsiDictConvertor<User, UserDictDTO> convertor = 
    DictClassHelperIocContext.getInstance().getConvertor(User.class);

// 执行转换
UserDictDTO dto = convertor.code2name(user);
```

## 架构优势

### 1. 类型安全
- 编译时类型检查
- 泛型支持
- 自动类型推断

### 2. 递归处理
- 自动处理嵌套对象
- 支持复杂集合类型
- 深度递归转换

### 3. 性能优化
- 单例IoC上下文
- 缓存映射关系
- 批量处理支持

### 4. 可扩展性
- 插件化转换器
- 自定义映射规则
- 灵活的配置选项

### 5. 向后兼容
- 保留原有DSL接口
- 渐进式迁移支持
- 兼容现有代码

## 总结

新的架构完全符合您的要求：
- ✅ LsiDictConvertor在核心包中供生成代码使用
- ✅ DictDTO不继承原实体，独立设计
- ✅ 递归处理嵌套对象和集合
- ✅ DictClassHelperIocContext管理所有映射关系
- ✅ 类型安全的转换器获取
- ✅ 完整的双向转换支持

这个架构提供了更好的类型安全性、可维护性和扩展性，同时保持了高性能和易用性。