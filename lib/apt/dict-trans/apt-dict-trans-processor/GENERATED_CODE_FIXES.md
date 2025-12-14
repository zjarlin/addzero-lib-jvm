# 生成代码修复指南

## 主要问题及修复方案

### 1. Import语句缺失

**问题**: 生成的代码缺少必要的import语句
**修复**: 在每个生成的Java文件顶部添加：

```java
// 对于Convertor类
import site.addzero.dict.trans.inter.LsiDictConvertor;

// 对于DictClassHelperIocContext
import site.addzero.dict.trans.inter.LsiDictConvertor;
```

### 2. 构造函数问题

**问题**: DictDTO类的构造函数参数不正确
**修复**: 使用静态工厂方法而不是构造函数：

```java
// 错误的用法
ComplexNestedEntityDictDTO dto = new ComplexNestedEntityDictDTO(entity);

// 正确的用法  
ComplexNestedEntityDictDTO dto = ComplexNestedEntityDictDTO.fromOriginal(entity);
```

### 3. 字段名不匹配

**问题**: 生成的setter/getter方法名与实际字段不匹配
**修复**: 确保字段名与方法名一致：

```java
// 如果字段是 gender_dictText
private String gender_dictText;

// 对应的方法应该是
public String getGender_dictText() { return gender_dictText; }
public void setGender_dictText(String value) { this.gender_dictText = value; }
```

### 4. 嵌套类型处理

**问题**: 嵌套类的DTO类型无法解析
**修复**: 确保所有嵌套类都生成了对应的DTO：

```java
// 原始类
public static class DeviceInfo {
    // ...
}

// 需要生成对应的DTO
public class DeviceInfoDictDTO {
    // ...
}
```

### 5. 方法参数问题

**问题**: SqlAssist类中的方法缺少参数
**修复**: 添加context参数：

```java
// 修复前
public static String getSystemDictSql() {

// 修复后  
public static String getSystemDictSql(Object context) {
```

### 6. transApi引用问题

**问题**: DictDsl类中引用了不存在的transApi字段
**修复**: 移除transApi相关代码或提供默认实现：

```java
// 移除或注释掉transApi相关代码
// transApi.translateDictBatchCode2name(...);

// 或者提供默认实现
// TODO: Implement dictionary translation
```

## 自动修复建议

### 处理器改进

1. **完善import生成**: 确保所有必要的import都被包含
2. **修复构造函数**: 使用正确的参数和静态工厂方法
3. **改进字段名生成**: 使用一致的命名规则
4. **嵌套类处理**: 正确处理嵌套类的DTO生成
5. **参数一致性**: 确保方法参数与实现一致

### 临时解决方案

在处理器修复之前，可以手动修改生成的代码：

1. 添加缺失的import语句
2. 修复构造函数调用
3. 调整字段名和方法名
4. 添加缺失的类定义
5. 修复方法参数

## 长期解决方案

1. **改进LSI字段分析**: 更准确地分析字段类型和嵌套关系
2. **完善代码模板**: 使用更健壮的代码生成模板
3. **添加验证**: 在生成代码后进行语法验证
4. **增强测试**: 添加更多的集成测试用例