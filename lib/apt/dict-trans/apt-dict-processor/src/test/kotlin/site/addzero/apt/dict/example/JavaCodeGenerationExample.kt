package site.addzero.apt.dict.example

import site.addzero.apt.dict.dsl.*
import site.addzero.apt.dict.template.JTETemplateManager

/**
 * Example demonstrating Java code generation from DSL
 * 
 * This example shows how the DSL is converted to Java code
 * for compile-time dictionary translation.
 */
class JavaCodeGenerationExample {
    
    fun generateUserEntityEnhancedJava(): String {
        val templateManager = JTETemplateManager()
        
        // Create DSL configuration for UserEntity
        val userEntityRule = EntityTranslationRule(
            entityName = "UserEntity",
            fieldRules = listOf(
                FieldTranslationRule(
                    fieldName = "status",
                    translationType = TranslationType.SYSTEM_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.SYSTEM_DICT,
                            dictCode = "user_status"
                        )
                    ),
                    targetFieldName = "statusText"
                ),
                FieldTranslationRule(
                    fieldName = "departmentId",
                    translationType = TranslationType.TABLE_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.TABLE_DICT,
                            table = "sys_department",
                            codeColumn = "id",
                            nameColumn = "dept_name"
                        )
                    ),
                    targetFieldName = "departmentText"
                )
            )
        )
        
        val dslConfig = DslTemplateConfig(
            entityClass = "UserEntity",
            translationRules = listOf(userEntityRule)
        )
        
        // Generate Java code
        return templateManager.renderEnhancedEntityJavaFromDsl(
            dslConfig = dslConfig,
            packageName = "com.example.entity",
            originalClassName = "UserEntity"
        )
    }
    
    fun generateComplexRBACJava(): String {
        val templateManager = JTETemplateManager()
        
        // Create complex RBAC DSL configuration
        val userEntityRule = EntityTranslationRule(
            entityName = "UserEntity",
            fieldRules = listOf(
                FieldTranslationRule(
                    fieldName = "status",
                    translationType = TranslationType.SYSTEM_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.SYSTEM_DICT,
                            dictCode = "user_status"
                        )
                    ),
                    targetFieldName = "statusText"
                ),
                FieldTranslationRule(
                    fieldName = "departmentId",
                    translationType = TranslationType.TABLE_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.TABLE_DICT,
                            table = "sys_department",
                            codeColumn = "id",
                            nameColumn = "dept_name"
                        )
                    ),
                    targetFieldName = "departmentText"
                )
            ),
            nestedRules = listOf(
                NestedTranslationRule(
                    fieldName = "roles",
                    targetType = "RoleEntity",
                    isCollection = true,
                    nestedRules = EntityTranslationRule(
                        entityName = "RoleEntity",
                        fieldRules = listOf(
                            FieldTranslationRule(
                                fieldName = "roleCode",
                                translationType = TranslationType.TABLE_DICT,
                                dictConfigs = listOf(
                                    DictConfig(
                                        type = TranslationType.TABLE_DICT,
                                        table = "sys_role",
                                        codeColumn = "role_code",
                                        nameColumn = "role_name"
                                    )
                                ),
                                targetFieldName = "roleText"
                            ),
                            FieldTranslationRule(
                                fieldName = "status",
                                translationType = TranslationType.SYSTEM_DICT,
                                dictConfigs = listOf(
                                    DictConfig(
                                        type = TranslationType.SYSTEM_DICT,
                                        dictCode = "role_status"
                                    )
                                ),
                                targetFieldName = "statusText"
                            )
                        )
                    )
                )
            )
        )
        
        val dslConfig = DslTemplateConfig(
            entityClass = "UserEntity",
            translationRules = listOf(userEntityRule)
        )
        
        // Generate Java code
        return templateManager.renderEnhancedEntityJavaFromDsl(
            dslConfig = dslConfig,
            packageName = "com.example.rbac",
            originalClassName = "UserEntity"
        )
    }
    
    /**
     * Expected Java output structure for reference
     */
    fun getExpectedJavaStructure(): String {
        return """
            package com.example.entity;
            
            import site.addzero.apt.dict.service.TransApi;
            import site.addzero.apt.dict.service.DictModel;
            import java.util.*;
            import java.util.concurrent.CompletableFuture;
            
            /**
             * Enhanced version of UserEntity with compile-time dictionary translation support.
             * 
             * Generated by JavaEntityEnhancer at compile time from DSL configuration.
             */
            public class UserEntityEnhanced {
                
                private String statusText;
                private String departmentText;
                
                public UserEntityEnhanced() {
                }
                
                public String getStatusText() {
                    return this.statusText;
                }
                
                public void setStatusText(String statusText) {
                    this.statusText = statusText;
                }
                
                public String getDepartmentText() {
                    return this.departmentText;
                }
                
                public void setDepartmentText(String departmentText) {
                    this.departmentText = departmentText;
                }
                
                public void translate(TransApi transApi, UserEntity sourceEntity) {
                    // System dictionary batch translation
                    String systemDictCodes = "user_status";
                    List<String> systemKeys = new ArrayList<>();
                    
                    if (sourceEntity.getStatus() != null) {
                        systemKeys.add(sourceEntity.getStatus().toString());
                    }
                    
                    if (!systemKeys.isEmpty()) {
                        List<DictModel> systemResults = transApi.translateDictBatchCode2name(systemDictCodes, String.join(",", systemKeys));
                        Map<String, DictModel> systemResultMap = new HashMap<>();
                        for (DictModel model : systemResults) {
                            systemResultMap.put(model.getDictCode() + ":" + model.getValue(), model);
                        }
                        
                        if (sourceEntity.getStatus() != null) {
                            String key = "user_status:" + sourceEntity.getStatus().toString();
                            DictModel result = systemResultMap.get(key);
                            if (result != null) {
                                this.setStatusText(result.getLabel());
                            }
                        }
                    }
                    
                    // Table dictionary batch translation
                    List<String> tableKeys_sys_department = new ArrayList<>();
                    
                    if (sourceEntity.getDepartmentId() != null) {
                        tableKeys_sys_department.add(sourceEntity.getDepartmentId().toString());
                    }
                    
                    if (!tableKeys_sys_department.isEmpty()) {
                        List<Map<String, Object>> tableResults_sys_department = transApi.translateTableBatchCode2name(
                            "sys_department", 
                            "dept_name", 
                            "id", 
                            String.join(",", tableKeys_sys_department)
                        );
                        Map<String, Map<String, Object>> tableResultMap_sys_department = new HashMap<>();
                        for (Map<String, Object> row : tableResults_sys_department) {
                            Object codeValue = row.get("id");
                            if (codeValue != null) {
                                tableResultMap_sys_department.put(codeValue.toString(), row);
                            }
                        }
                        
                        if (sourceEntity.getDepartmentId() != null) {
                            String key = sourceEntity.getDepartmentId().toString();
                            Map<String, Object> result = tableResultMap_sys_department.get(key);
                            if (result != null) {
                                Object nameValue = result.get("dept_name");
                                if (nameValue != null) {
                                    this.setDepartmentText(nameValue.toString());
                                }
                            }
                        }
                    }
                }
                
                public CompletableFuture<Void> translateAsync(TransApi transApi, UserEntity sourceEntity) {
                    return CompletableFuture.runAsync(() -> translate(transApi, sourceEntity));
                }
                
                public void populateFromContext(Map<String, Object> context) {
                    Object statusTextValue = context.get("statusText");
                    if (statusTextValue instanceof String) {
                        this.setStatusText((String) statusTextValue);
                    }
                    Object departmentTextValue = context.get("departmentText");
                    if (departmentTextValue instanceof String) {
                        this.setDepartmentText((String) departmentTextValue);
                    }
                }
                
                public static final String DSL_CONFIG = "UserEntity";
                
                public static final Set<String> SYSTEM_DICT_CODES = new HashSet<String>() {{
                    add("user_status");
                }};
                
                public static final Set<String> TABLE_DICT_CONFIGS = new HashSet<String>() {{
                    add("sys_department:id:dept_name");
                }};
            }
        """.trimIndent()
    }
}