# LSI-KSP模块

## 概述

LSI-KSP是LSI（Language Structure Interface）系统的KSP（Kotlin Symbol Processing）实现模块。它提供了KSP符号到LSI接口的适配器实现，使得KSP处理器能够使用与APT处理器相同的统一API来访问代码结构信息。

## 功能特性

- **统一接口**: 提供与LSI-APT相同的接口实现
- **KSP符号包装**: 将KSP符号（KSClassDeclaration、KSPropertyDeclaration等）包装为LSI对象
- **Kotlin特性支持**: 正确处理Kotlin特有的语言特性（数据类、可空性、扩展函数等）
- **扩展函数**: 提供便利的转换扩展函数
- **错误处理**: 提供健壮的错误处理和降级策略

## 核心组件

### 适配器类
- `KspLsiClass` - 类结构适配器
- `KspLsiField` - 字段结构适配器  
- `KspLsiMethod` - 方法结构适配器
- `KspLsiType` - 类型结构适配器
- `KspLsiAnnotation` - 注解结构适配器

### 扩展函数
- `KSClassDeclaration.toLsiClass()`
- `KSPropertyDeclaration.toLsiField()`
- `KSFunctionDeclaration.toLsiMethod()`
- `KSType.toLsiType()`
- `KSAnnotation.toLsiAnnotation()`

## 使用示例

```kotlin
// 在KSP处理器中使用
class MyKspProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    
    private val lsiLogger = logger.toLsiLogger()
    
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.example.MyAnnotation")
        
        symbols.filterIsInstance<KSClassDeclaration>().forEach { ksClass ->
            // 使用LSI统一接口
            val lsiClass = ksClass.toLsiClass(resolver)
            
            // 现在可以使用与APT处理器相同的API
            val className = lsiClass.name
            val fields = lsiClass.fields
            val methods = lsiClass.methods
            
            // 使用统一的日志接口
            lsiLogger.info("Processing class: $className")
            
            // 进行代码生成...
        }
        
        return emptyList()
    }
}
```

## 依赖关系

- `lsi-core`: LSI核心接口定义
- `symbol-processing-api`: KSP API
- `tool-str`: 字符串工具库

## 测试

模块包含完整的测试套件：
- 单元测试：验证具体功能和边界情况
- 属性测试：验证通用正确性属性
- 兼容性测试：确保与LSI-APT的接口一致性