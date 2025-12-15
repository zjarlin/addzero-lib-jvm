# 编译时翻译架构设计

## 核心理念

完全基于编译时信息生成代码，运行时只进行值收集和批量查询，彻底避免反射和运行时配置解析。

## 架构组件

### 1. DictTranslationConfig - 编译时配置
```kotlin
data class DictTranslationConfig(
    val fieldPath: String,              // "deviceInfo.location.testvar1"
    val dictType: String,               // "system" or "table"
    val dictConfig: String,             // "sys_user_sex" or "equipment|id|name|"
    val targetFieldPath: String,        // "deviceInfo.location.testvar1Name"
    val nestedClassPrefix: String = ""  // "ComplexNestedEntity.DeviceInfo.Location"
)
```

### 2. BatchTranslationExecutor - 批量执行器
```kotlin
class BatchTranslationExecutor {
    fun executeBatchTranslation(
        configs: List<DictTranslationConfig>,    // 编译时配置
        codeValues: Map<String, Set<String>>     // 运行时收集的值
    ): CompletableFuture<Map<String, Map<String, String>>>
}
```

## 生成的代码示例

### 对于 ComplexNestedEntity

```java
public class ComplexNestedEntityConvertor implements LsiDictConvertor<ComplexNestedEntity, ComplexNestedEntityDictDTO> {
    
    private final BatchTranslationExecutor batchExecutor;
    
    public List<ComplexNestedEntityDictDTO> code2name(List<ComplexNestedEntity> entities) {
        // 转换为DTO
        List<ComplexNestedEntityDictDTO> dtos = entities.stream()
            .map(ComplexNestedEntityDictDTO::new)
            .collect(Collectors.toList());
        
        // 收集需要翻译的值
        Map<String, Set<String>> codeValues = collectCodeValues(dtos);
        
        // 执行批量翻译
        Map<String, Map<String, String>> translationResults = 
            batchExecutor.executeBatchTranslation(getTranslationConfigs(), codeValues).get();
        
        // 应用翻译结果
        applyTranslationsWithGeneratedCode(dtos, translationResults);
        
        return dtos;
    }
    
    /**
     * 编译时生成的翻译配置
     */
    private List<DictTranslationConfig> getTranslationConfigs() {
        List<DictTranslationConfig> configs = new ArrayList<>();
        
        // 根级字段配置
        configs.add(new DictTranslationConfig(
            "gender",
            "system",
            "sys_user_sex",
            "genderName",
            ""
        ));
        
        configs.add(new DictTranslationConfig(
            "deviceStatus",
            "system",
            "sys_show_hide",
            "deviceStatusName",
            ""
        ));
        
        configs.add(new DictTranslationConfig(
            "productKey",
            "table",
            "iot_product|product_key|product_name|",
            "productKeyName",
            ""
        ));
        
        // 嵌套字段配置
        configs.add(new DictTranslationConfig(
            "deviceInfo.deviceId",
            "table",
            "equipment|id|code|",
            "deviceInfo.deviceIdName",
            "ComplexNestedEntity.DeviceInfo"
        ));
        
        configs.add(new DictTranslationConfig(
            "deviceInfo.deviceId1",
            "table",
            "equipment|id|name|",
            "deviceInfo.deviceId1Name",
            "ComplexNestedEntity.DeviceInfo"
        ));
        
        // 深度嵌套字段配置
        configs.add(new DictTranslationConfig(
            "deviceInfo.location.testvar1",
            "system",
            "sys_normal_disable",
            "deviceInfo.location.testvar1Name",
            "ComplexNestedEntity.DeviceInfo.Location"
        ));
        
        configs.add(new DictTranslationConfig(
            "deviceInfo.location.testvar2",
            "system",
            "sys_normal_disable",
            "deviceInfo.location.testvar2Name",
            "ComplexNestedEntity.DeviceInfo.Location"
        ));
        
        configs.add(new DictTranslationConfig(
            "deviceInfo.location.testTableVar",
            "table",
            "equipment|id|code|",
            "deviceInfo.location.testTableVarName",
            "ComplexNestedEntity.DeviceInfo.Location"
        ));
        
        return configs;
    }
    
    /**
     * 收集需要翻译的代码值
     */
    private Map<String, Set<String>> collectCodeValues(List<ComplexNestedEntityDictDTO> dtos) {
        Map<String, Set<String>> codeValues = new HashMap<>();
        
        for (ComplexNestedEntityDictDTO dto : dtos) {
            // 收集根级字段值
            Object genderValue = dto.getGender();
            if (genderValue != null) {
                codeValues.computeIfAbsent("system:sys_user_sex", k -> new HashSet<>())
                    .add(genderValue.toString());
            }
            
            Object deviceStatusValue = dto.getDeviceStatus();
            if (deviceStatusValue != null) {
                codeValues.computeIfAbsent("system:sys_show_hide", k -> new HashSet<>())
                    .add(deviceStatusValue.toString());
            }
            
            Object productKeyValue = dto.getProductKey();
            if (productKeyValue != null) {
                codeValues.computeIfAbsent("table:iot_product|product_key|product_name|", k -> new HashSet<>())
                    .add(productKeyValue.toString());
            }
            
            // 收集嵌套字段值
            Object deviceIdValue = dto.getDeviceInfo() != null ? dto.getDeviceInfo().getDeviceId() : null;
            if (deviceIdValue != null) {
                codeValues.computeIfAbsent("table:equipment|id|code|", k -> new HashSet<>())
                    .add(deviceIdValue.toString());
            }
            
            Object deviceId1Value = dto.getDeviceInfo() != null ? dto.getDeviceInfo().getDeviceId1() : null;
            if (deviceId1Value != null) {
                codeValues.computeIfAbsent("table:equipment|id|name|", k -> new HashSet<>())
                    .add(deviceId1Value.toString());
            }
            
            // 收集深度嵌套字段值
            Object testvar1Value = dto.getDeviceInfo() != null && 
                                  dto.getDeviceInfo().getLocation() != null ? 
                                  dto.getDeviceInfo().getLocation().getTestvar1() : null;
            if (testvar1Value != null) {
                codeValues.computeIfAbsent("system:sys_normal_disable", k -> new HashSet<>())
                    .add(testvar1Value.toString());
            }
            
            Object testvar2Value = dto.getDeviceInfo() != null && 
                                  dto.getDeviceInfo().getLocation() != null ? 
                                  dto.getDeviceInfo().getLocation().getTestvar2() : null;
            if (testvar2Value != null) {
                codeValues.computeIfAbsent("system:sys_normal_disable", k -> new HashSet<>())
                    .add(testvar2Value.toString());
            }
            
            Object testTableVarValue = dto.getDeviceInfo() != null && 
                                      dto.getDeviceInfo().getLocation() != null ? 
                                      dto.getDeviceInfo().getLocation().getTestTableVar() : null;
            if (testTableVarValue != null) {
                codeValues.computeIfAbsent("table:equipment|id|code|", k -> new HashSet<>())
                    .add(testTableVarValue.toString());
            }
        }
        
        return codeValues;
    }
    
    /**
     * 应用翻译结果
     */
    private void applyTranslationsWithGeneratedCode(
        List<ComplexNestedEntityDictDTO> dtos, 
        Map<String, Map<String, String>> translationResults
    ) {
        for (ComplexNestedEntityDictDTO dto : dtos) {
            // 应用根级字段翻译
            Object genderValue = dto.getGender();
            if (genderValue != null) {
                Map<String, String> genderDict = translationResults.get("system:sys_user_sex");
                if (genderDict != null) {
                    String translatedValue = genderDict.get(genderValue.toString());
                    if (translatedValue != null) {
                        dto.setGenderName(translatedValue);
                    }
                }
            }
            
            Object deviceStatusValue = dto.getDeviceStatus();
            if (deviceStatusValue != null) {
                Map<String, String> deviceStatusDict = translationResults.get("system:sys_show_hide");
                if (deviceStatusDict != null) {
                    String translatedValue = deviceStatusDict.get(deviceStatusValue.toString());
                    if (translatedValue != null) {
                        dto.setDeviceStatusName(translatedValue);
                    }
                }
            }
            
            Object productKeyValue = dto.getProductKey();
            if (productKeyValue != null) {
                Map<String, String> productKeyDict = translationResults.get("table:iot_product|product_key|product_name|");
                if (productKeyDict != null) {
                    String translatedValue = productKeyDict.get(productKeyValue.toString());
                    if (translatedValue != null) {
                        dto.setProductKeyName(translatedValue);
                    }
                }
            }
            
            // 应用嵌套字段翻译
            Object deviceIdValue = dto.getDeviceInfo() != null ? dto.getDeviceInfo().getDeviceId() : null;
            if (deviceIdValue != null) {
                Map<String, String> deviceIdDict = translationResults.get("table:equipment|id|code|");
                if (deviceIdDict != null) {
                    String translatedValue = deviceIdDict.get(deviceIdValue.toString());
                    if (translatedValue != null) {
                        if (dto.getDeviceInfo() != null) { 
                            dto.getDeviceInfo().setDeviceIdName(translatedValue); 
                        }
                    }
                }
            }
            
            // 应用深度嵌套字段翻译
            Object testvar1Value = dto.getDeviceInfo() != null && 
                                  dto.getDeviceInfo().getLocation() != null ? 
                                  dto.getDeviceInfo().getLocation().getTestvar1() : null;
            if (testvar1Value != null) {
                Map<String, String> testvar1Dict = translationResults.get("system:sys_normal_disable");
                if (testvar1Dict != null) {
                    String translatedValue = testvar1Dict.get(testvar1Value.toString());
                    if (translatedValue != null) {
                        if (dto.getDeviceInfo() != null && dto.getDeviceInfo().getLocation() != null) { 
                            dto.getDeviceInfo().getLocation().setTestvar1Name(translatedValue); 
                        }
                    }
                }
            }
        }
    }
}
```

## 执行流程

### 1. 编译时
1. 扫描 `@Dict` 注解
2. 生成 `DictTranslationConfig` 配置
3. 生成字段访问和设置代码
4. 生成批量查询逻辑

### 2. 运行时
1. 转换实体为DTO
2. 使用生成的访问器收集需要翻译的值
3. 按配置键分组，执行批量查询
4. 使用生成的设置器应用翻译结果

## 性能优势

### 1. 编译时优化
- 所有配置在编译时确定
- 无运行时反射开销
- 类型安全的字段访问

### 2. 批量查询优化
- 智能去重相同配置的查询
- 预编译SQL模板
- 最小化数据库交互

### 3. 内存优化
- 无需保存对象引用
- 及时释放临时数据结构
- 最小化内存分配

## SQL 执行示例

### 系统字典批量查询
```sql
-- 原来：多次单独查询
SELECT dict_name FROM sys_dict_data WHERE dict_type = 'sys_user_sex' AND dict_code = '0';
SELECT dict_name FROM sys_dict_data WHERE dict_type = 'sys_user_sex' AND dict_code = '1';
SELECT dict_name FROM sys_dict_data WHERE dict_type = 'sys_show_hide' AND dict_code = '0';

-- 现在：批量查询
SELECT dict_code as code, dict_name as name FROM sys_dict_data 
WHERE dict_type = 'sys_user_sex' AND dict_code IN ('0','1') AND status = '0';

SELECT dict_code as code, dict_name as name FROM sys_dict_data 
WHERE dict_type = 'sys_show_hide' AND dict_code IN ('0','1') AND status = '0';
```

### 表字典批量查询
```sql
-- 原来：多次单独查询
SELECT name FROM equipment WHERE id = '49';
SELECT name FROM equipment WHERE id = '55';

-- 现在：批量查询
SELECT id as code, name as name FROM equipment WHERE id IN ('49','55');
```

这种架构实现了最佳的性能和可维护性平衡，完全消除了反射开销，同时保持了代码的清晰性和类型安全性。