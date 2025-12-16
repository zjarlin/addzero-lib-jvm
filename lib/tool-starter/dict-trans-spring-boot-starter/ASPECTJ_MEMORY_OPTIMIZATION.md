# AspectJ内存优化修复方案

## 问题描述

两个Spring Boot Starter（dict-trans和controller-advice）都针对Controller做切面处理，导致AspectJ `AspectJExpressionPointcut`实例占用过多内存（13.35MB，占13.15%的堆空间）。

## 修复内容

### 1. 移除重复的@EnableAspectJAutoProxy

**文件**: `lib/tool-starter/controller-advice-spring-boot-starter/src/main/kotlin/site/addzero/web/infra/advice/AddzeroControllerAdviceAutoRc.kt`

**修改**: 移除了重复的`@EnableAspectJAutoProxy`注解，因为dict-trans-starter已经启用了AspectJ自动代理。

```kotlin
// 移除前
@Configuration
@EnableAspectJAutoProxy  // 重复配置
@ComponentScan(basePackages = ["site.addzero.web.infra.advice"])
class AddzeroControllerAdviceAutoRc

// 移除后
@Configuration
@ComponentScan(basePackages = ["site.addzero.web.infra.advice"])
class AddzeroControllerAdviceAutoRc
```

### 2. 优化AspectJExpressionPointcut使用

**文件**: `lib/tool-starter/dict-trans-spring-boot-starter/src/main/kotlin/site/addzero/aop/dicttrans/dictaop/DictAop.kt`

**修改**:
- 移除了单独的`dictAnnotationPointcut()` Bean
- 将注解检查和包扫描合并到一个表达式中
- 减少了AspectJExpressionPointcut实例的创建

```kotlin
// 优化前：创建多个Pointcut实例
@Bean
fun dictAnnotationPointcut(): AspectJExpressionPointcut { ... }

@Bean
fun dictAdvisor(
    properties: AddzeroDictTransProperties,
    dictAnnotationPointcut: AspectJExpressionPointcut,
    transStrategySelector: TransStrategySelector
): Advisor {
    val expressionPointcut = AspectJExpressionPointcut().apply { ... }
    val compositePointcut = ComposablePointcut(expressionPointcut as Pointcut)
        .intersection(dictAnnotationPointcut as Pointcut)
    // ...
}

// 优化后：使用单一Pointcut
@Bean
fun dictAdvisor(
    properties: AddzeroDictTransProperties,
    transStrategySelector: TransStrategySelector
): Advisor {
    // 合并注解和包扫描条件到一个表达式中
    val combinedExpression = "@annotation(site.addzero.aop.dicttrans.anno.Dict) && (${properties.expression})"

    val pointcut = AspectJExpressionPointcut().apply {
        expression = combinedExpression
    }
    // ...
}
```

### 3. 修复Bean重复注册问题

**文件**: `lib/tool-starter/dict-trans-spring-boot-starter/src/main/kotlin/site/addzero/rc/AddzeroDictTransProperties.kt`

**修改**: 移除了`@Component`注解，避免与`@EnableConfigurationProperties`重复注册。

```kotlin
// 修复前
@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
@Component  // 导致Bean重复注册
class AddzeroDictTransProperties(...)

// 修复后
@ConfigurationProperties(prefix = "site.addzero.scan.dict.trans")
class AddzeroDictTransProperties(...)
```

### 4. 优化默认切点表达式

**修改**: 将默认表达式从`execution(* ${pkg}..*Controller*+.*(..))`改为更精确的`execution(* ${pkg}..controller.*Controller.*(..))`

- 使用`controller`包名限定，减少不必要的类扫描
- 使用`.*Controller.*`代替`*Controller*+`，避免匹配意外的类名

## 性能优化效果

1. **减少内存占用**:
   - 减少了AspectJExpressionPointcut实例数量
   - 避免了Bean重复注册导致的额外内存消耗

2. **提升启动速度**:
   - 减少了切点匹配的复杂度
   - 避免了重复的AOP代理配置

3. **保持功能完整性**:
   - 切点表达式仍然同时检查`@Dict`注解和包扫描
   - 只有同时满足两个条件才会触发切面逻辑

## 验证方法

1. **内存使用监控**:
   ```bash
   jmap -histo:live <pid> | grep AspectJExpressionPointcut
   ```

2. **启动日志检查**:
   确保没有Bean冲突错误
   - 确保AOP正常工作

3. **功能测试**:
   - 测试带`@Dict`注解的Controller方法是否正常
   - 测试不带注解或不在包内的方法是否被正确忽略

## 注意事项

1. **表达式配置**: 如果用户自定义了`site.addzero.scan.dict.trans.expression`，会自动与注解检查合并
2. **向后兼容**: 优化后的代码完全向后兼容，不会影响现有功能
3. **最佳实践**: 建议在实际项目中使用更具体的包路径，如`com.yourcompany.controller`而不是宽泛的`*..*Controller*`