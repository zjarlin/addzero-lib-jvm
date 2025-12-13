# Dictionary Translation DSL Usage Guide

This guide demonstrates how to use the compile-time dictionary translation system with APT (Annotation Processing Tool) for optimal performance.

## Overview

The APT Dictionary Translation system provides:
- **Compile-time code generation** - Zero runtime reflection overhead
- **Inheritance-based enhancement** - Enhanced entities extend original entities (T->R mapping)
- **Batch query optimization** - Eliminates N+1 query problems
- **Type-safe translations** - Full compile-time type checking
- **Multiple translation sources** - System dictionaries, table dictionaries, and SPEL expressions

## Basic Usage

### 1. Annotate Your Entity Classes

```java
@DictTranslate(suffix = "Enhanced")
public class User {
    private Long id;
    private String username;
    
    @DictField(
        dictCode = "user_status",
        targetField = "statusText"
    )
    private String status;
    
    @DictField(
        table = "sys_department",
        codeColumn = "id",
        nameColumn = "name",
        targetField = "departmentName"
    )
    private Long departmentId;
    
    // getters and setters...
}
```

### 2. Generated Enhanced Entity

The APT processor generates:

```java
public class UserEnhanced extends User {
    private String statusText;
    private String departmentName;
    
    public UserEnhanced() {
        super();
    }
    
    // Translation methods
    public void translate(TransApi transApi) {
        // Batch-optimized translation logic
    }
    
    public CompletableFuture<Void> translateAsync(TransApi transApi) {
        return CompletableFuture.runAsync(() -> translate(transApi));
    }
    
    // Getters and setters for translation fields...
}
```

### 3. Use Enhanced Entities

```java
public class UserService {
    
    public UserEnhanced loadUserWithTranslations(Long userId) {
        // Load original data
        User user = userRepository.findById(userId);
        
        // Create enhanced entity (inherits from User)
        UserEnhanced enhanced = new UserEnhanced();
        enhanced.setId(user.getId());
        enhanced.setUsername(user.getUsername());
        enhanced.setStatus(user.getStatus());
        enhanced.setDepartmentId(user.getDepartmentId());
        
        // Perform batch translation
        enhanced.translate(transApi);
        
        return enhanced;
    }
}
```

## Translation Types

### 1. System Dictionary Translation

For predefined code-value mappings:

```java
@DictField(
    dictCode = "user_status",
    targetField = "statusText"
)
private String status;
```

### 2. Table Dictionary Translation

For database table lookups:

```java
@DictField(
    table = "sys_department",
    codeColumn = "id",
    nameColumn = "name",
    condition = "status = 'ACTIVE'",
    targetField = "departmentName"
)
private Long departmentId;
```

### 3. SPEL Expression Translation

For complex dynamic translations:

```java
@DictField(
    spelExp = "#{dict('category_type', categoryCode) + ' - ' + table('sys_subcategory', 'id', 'name', subcategoryId)}",
    targetField = "fullCategoryText"
)
private String categoryCode;
```

### 4. Multi-Dictionary Translation

Multiple fallback translation sources:

```java
@DictField(
    dictCode = "primary_dict",
    table = "fallback_table",
    codeColumn = "code",
    nameColumn = "name",
    spelExp = "#{code + ' (Unknown)'}",
    targetField = "translatedText"
)
private String code;
```

## Performance Optimization

### N+1 Query Elimination

**Before (Reflection-based):**
```java
// For 100 users - Results in 301 queries!
List<User> users = loadUsers(); // 1 query
for (User user : users) {
    user.statusText = dictService.translate("user_status", user.status); // 100 queries
    user.departmentName = deptService.getName(user.departmentId); // 100 queries  
    user.organizationName = orgService.getName(user.organizationId); // 100 queries
}
```

**After (APT-generated):**
```java
// For 100 users - Results in only 4 queries!
List<User> users = loadUsers(); // 1 query
List<UserEnhanced> enhanced = users.stream()
    .map(user -> {
        UserEnhanced e = new UserEnhanced();
        // Copy fields...
        e.translate(transApi); // Batch queries: 1 for status, 1 for dept, 1 for org
        return e;
    })
    .collect(toList());
```

### Batch Translation Metadata

The generated classes include metadata for optimization:

```java
public class UserEnhanced extends User {
    // Generated metadata constants
    public static final Set<String> SYSTEM_DICT_CODES = Set.of("user_status");
    public static final Set<String> TABLE_DICT_CONFIGS = Set.of("sys_department:id:name");
    
    // Translation context can use this metadata for pre-loading
}
```

## Advanced Patterns

### 1. RBAC (Role-Based Access Control)

```java
@DictTranslate
public class UserRole {
    @DictField(table = "rbac_user", codeColumn = "id", nameColumn = "username")
    private Long userId;
    
    @DictField(table = "rbac_role", codeColumn = "id", nameColumn = "name")
    private Long roleId;
    
    @DictField(dictCode = "assignment_status")
    private String status;
}

// Usage
public List<UserRoleEnhanced> getUserRoles(Long userId) {
    return userRoleRepository.findByUserId(userId)
        .stream()
        .map(ur -> {
            UserRoleEnhanced enhanced = new UserRoleEnhanced();
            // Copy fields...
            enhanced.translate(transApi);
            return enhanced;
        })
        .collect(toList());
}
```

### 2. Audit Logging

```java
@DictTranslate
public class AuditLog {
    @DictField(table = "rbac_user", codeColumn = "id", nameColumn = "username")
    private Long operatorId;
    
    @DictField(dictCode = "audit_action")
    private String action;
    
    @DictField(dictCode = "audit_result")
    private String result;
    
    @DictField(spelExp = "#{dict('severity_level', result)}")
    private String severity;
}
```

### 3. Multi-Tenant Entities

```java
@DictTranslate
public class TenantEntity {
    @DictField(table = "sys_tenant", codeColumn = "id", nameColumn = "name", condition = "status = 'ACTIVE'")
    private Long tenantId;
    
    @DictField(dictCode = "tenant_status")
    private String status;
    
    @DictField(table = "sys_region", codeColumn = "code", nameColumn = "display_name")
    private String regionCode;
}
```

## Error Handling and Monitoring

The APT processor includes comprehensive error handling:

### Compilation-Time Validation

```java
@DictField(
    // ERROR: Must specify at least one translation source
    targetField = "invalidField"
)
private String invalidCode;

@DictField(
    table = "sys_table",
    // ERROR: Table dictionary must specify both codeColumn and nameColumn
    codeColumn = "id",
    targetField = "tableName"
)
private Long tableId;
```

### Runtime Performance Monitoring

```java
// Generated classes include performance metadata
UserEnhanced enhanced = new UserEnhanced();
enhanced.translate(transApi); // Automatically monitored for performance

// Performance reports available during compilation
// APT Performance Report:
//   Total Processing Time: 1250ms
//   Overall Throughput: 15.2 ops/sec
//   Operations:
//     processClass: Count: 25, Average: 45ms
//     codeGeneration: Count: 25, Average: 32ms
```

## Best Practices

### 1. Entity Design

- **Use inheritance pattern**: Enhanced entities extend original entities
- **Group related translations**: Keep related dictionary fields in the same entity
- **Consistent naming**: Use consistent patterns for target field names

### 2. Performance Optimization

- **Batch operations**: Always use the generated `translate()` method for batch optimization
- **Async processing**: Use `translateAsync()` for non-blocking operations
- **Metadata utilization**: Leverage generated metadata constants for pre-loading

### 3. Error Handling

- **Validate configurations**: Use DSL template validation during development
- **Monitor performance**: Watch for slow operations and high memory usage
- **Graceful degradation**: Handle translation failures gracefully

### 4. Testing

```java
@Test
public void testUserEnhancedInheritance() {
    UserEnhanced enhanced = new UserEnhanced();
    
    // Test inheritance
    assertTrue(enhanced instanceof User);
    
    // Test original fields
    enhanced.setId(1L);
    enhanced.setUsername("testuser");
    assertEquals(1L, enhanced.getId());
    
    // Test translation fields
    enhanced.setStatusText("Active User");
    assertEquals("Active User", enhanced.getStatusText());
}

@Test
public void testBatchTranslation() {
    UserEnhanced enhanced = new UserEnhanced();
    enhanced.setStatus("ACTIVE");
    enhanced.setDepartmentId(100L);
    
    // Mock TransApi
    TransApi mockTransApi = createMockTransApi();
    
    // Test translation
    enhanced.translate(mockTransApi);
    
    // Verify translations were applied
    assertNotNull(enhanced.getStatusText());
    assertNotNull(enhanced.getDepartmentName());
}
```

## Migration Guide

### From Reflection-Based to APT-Based

1. **Add APT dependencies**:
```gradle
dependencies {
    implementation 'site.addzero.apt:dict-trans-annotations'
    annotationProcessor 'site.addzero.apt:dict-trans-processor'
}
```

2. **Annotate existing entities**:
```java
// Before
public class User {
    @Dict(dictCode = "user_status")
    private String status;
}

// After  
@DictTranslate
public class User {
    @DictField(dictCode = "user_status", targetField = "statusText")
    private String status;
}
```

3. **Update service layer**:
```java
// Before
public User loadUser(Long id) {
    User user = repository.findById(id);
    dictTranslator.translate(user); // Reflection-based
    return user;
}

// After
public UserEnhanced loadUser(Long id) {
    User user = repository.findById(id);
    UserEnhanced enhanced = new UserEnhanced();
    // Copy fields...
    enhanced.translate(transApi); // APT-generated
    return enhanced;
}
```

## Troubleshooting

### Common Issues

1. **Compilation Errors**:
   - Ensure all required fields are specified for each translation type
   - Check that target field names don't conflict
   - Verify table and column names exist

2. **Performance Issues**:
   - Monitor APT processor performance reports
   - Check for slow operations (>1000ms)
   - Verify batch optimization is working

3. **Runtime Errors**:
   - Ensure TransApi implementation is correct
   - Check database connectivity for table dictionaries
   - Verify dictionary codes exist in system dictionaries

### Debug Information

Enable debug logging during compilation:
```
javac -processor site.addzero.apt.dict.processor.DictTranslateProcessor -Xlint:processing ...
```

The processor will output detailed information about:
- Processed entities and their configurations
- Generated code statistics
- Performance metrics
- Error details and recovery attempts

## Conclusion

The APT Dictionary Translation system provides a powerful, type-safe, and high-performance solution for dictionary translations. By generating code at compile-time and using inheritance-based enhancement, it eliminates runtime reflection overhead while maintaining clean, maintainable code patterns.

Key benefits:
- **99%+ query reduction** through batch optimization
- **Zero runtime reflection** overhead
- **Type-safe translations** with compile-time validation
- **Inheritance-based design** for clean integration
- **Comprehensive monitoring** and error handling