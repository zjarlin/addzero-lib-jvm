# ServiceLoader in KSP Demo

这个demo演示了如何在KSP（Kotlin Symbol Processing）阶段使用ServiceLoader。

## 项目结构

- `processor/` - KSP处理器模块，包含：
  - `CodeGeneratorService` - 服务接口
  - `DefaultCodeGenerator` - 默认实现
  - `AnnotationCodeGenerator` - 注解处理实现
  - `ServiceLoaderProcessor` - 使用ServiceLoader的KSP处理器
  - `META-INF/services/` - ServiceLoader配置文件

- `test/` - 测试模块，使用KSP处理器

## 工作原理

1. **ServiceLoader配置**
   - `META-INF/services/site.addzero.ksp.servicoloader.CodeGeneratorService` 文件列出了所有实现类
   - ServiceLoader会在编译时自动加载这些实现

2. **KSP处理器**
   - `ServiceLoaderProcessor`在编译阶段运行
   - 使用ServiceLoader加载所有`CodeGeneratorService`实现
   - 为每个发现的实现生成代码

## 测试步骤

1. 构建processor模块：
```bash
./gradlew :lib:ksp:serviceloader-demo:processor:build
```

2. 构建test模块（会触发KSP处理）：
```bash
./gradlew :lib:ksp:serviceloader-demo:test:build
```

3. 查看生成的代码：
   - `build/generated/ksp/` 目录下会包含生成的文件
   - `ServiceRegistry.kt` - 列出所有发现的服务
   - 为每个测试类生成的代码文件

## 预期输出

编译时会看到日志输出：
```
ServiceLoaderProcessor: Starting processing
ServiceLoaderProcessor: Found CodeGeneratorService implementations:
  - DefaultGenerator
  - AnnotationGenerator
```

生成的`ServiceRegistry.kt`示例：
```kotlin
object ServiceRegistry {
    val availableServices = listOf<String>(
        "DefaultGenerator",
        "AnnotationGenerator",
    )

    fun getServiceNames(): List<String> = availableServices
}
```

## 关键点

- ServiceLoader在KSP阶段可以正常工作
- 需要在META-INF/services中正确配置
- 所有服务实现都必须在classpath中
- 适用于可插拔的处理器架构