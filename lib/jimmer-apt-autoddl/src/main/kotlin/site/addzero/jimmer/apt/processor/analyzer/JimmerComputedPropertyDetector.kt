package site.addzero.jimmer.apt.processor.analyzer

import site.addzero.util.lsi.field.LsiField
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi.clazz.LsiClass

/**
 * Jimmer计算属性检测器
 * 
 * 负责识别和过滤Jimmer实体中的计算属性，确保它们不会生成数据库列
 */
class JimmerComputedPropertyDetector {

    /**
     * 检测字段是否为计算属性
     */
    fun isComputedProperty(lsiField: LsiField): Boolean {
        return isFormulaProperty(lsiField) ||
               isIdViewProperty(lsiField) ||
               isTransientProperty(lsiField) ||
               isComputedGetter(lsiField)
    }

    /**
     * 检测字段是否应该从DDL生成中排除
     */
    fun shouldExcludeFromDDL(lsiField: LsiField): Boolean {
        return isComputedProperty(lsiField) ||
               isStaticField(lsiField) ||
               isCollectionField(lsiField) ||
               isNonPersistentRelationship(lsiField)
    }

    /**
     * 获取所有应该包含在DDL中的字段
     */
    fun getPersistentFields(lsiClass: LsiClass): List<LsiField> {
        return lsiClass.fields.filter { field ->
            !shouldExcludeFromDDL(field)
        }
    }

    /**
     * 检测实体中的所有计算属性
     */
    fun detectComputedProperties(lsiClass: LsiClass): List<ComputedPropertyInfo> {
        val computedProperties = mutableListOf<ComputedPropertyInfo>()
        
        // 检测字段级别的计算属性
        lsiClass.fields.forEach { field ->
            if (isComputedProperty(field)) {
                computedProperties.add(
                    ComputedPropertyInfo(
                        name = field.name ?: "unknown",
                        type = determineComputedPropertyType(field),
                        formula = extractFormula(field),
                        idView = extractIdView(field),
                        isTransient = isTransientProperty(field)
                    )
                )
            }
        }
        
        // 检测方法级别的计算属性
        lsiClass.methods.forEach { method ->
            if (isComputedGetterMethod(method)) {
                computedProperties.add(
                    ComputedPropertyInfo(
                        name = extractPropertyNameFromGetter(method),
                        type = ComputedPropertyType.COMPUTED_GETTER,
                        formula = extractFormulaFromMethod(method),
                        idView = null,
                        isTransient = false
                    )
                )
            }
        }
        
        return computedProperties
    }

    // ========== 私有辅助方法 ==========

    /**
     * 检测@Formula注解的属性
     */
    private fun isFormulaProperty(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" ||
            annotation.simpleName == "Formula"
        }
    }

    /**
     * 检测@IdView注解的属性
     */
    private fun isIdViewProperty(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.IdView" ||
            annotation.simpleName == "IdView"
        }
    }

    /**
     * 检测@Transient注解的属性
     */
    private fun isTransientProperty(lsiField: LsiField): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName == "java.beans.Transient" ||
            annotation.qualifiedName == "javax.persistence.Transient" ||
            annotation.simpleName == "Transient"
        }
    }

    /**
     * 检测计算getter方法
     */
    private fun isComputedGetter(lsiField: LsiField): Boolean {
        // 在Kotlin中，计算属性通常表现为只有getter的属性
        // 这里需要根据LSI的具体实现来判断
        
        // 检查字段是否有自定义getter但没有backing field
        // 这通常表示它是一个计算属性
        
        // 暂时使用简单的启发式规则
        val fieldName = lsiField.name ?: return false
        
        // 如果字段名以"computed"、"calculated"等开头，可能是计算属性
        return fieldName.startsWith("computed") ||
               fieldName.startsWith("calculated") ||
               fieldName.startsWith("derived")
    }

    /**
     * 检测静态字段
     */
    private fun isStaticField(lsiField: LsiField): Boolean {
        return lsiField.isStatic
    }

    /**
     * 检测集合字段
     */
    private fun isCollectionField(lsiField: LsiField): Boolean {
        return lsiField.type?.isCollectionType == true
    }

    /**
     * 检测非持久化关系字段
     */
    private fun isNonPersistentRelationship(lsiField: LsiField): Boolean {
        // OneToMany和ManyToMany关系不需要在源表中创建列
        return hasAnnotation(lsiField, "OneToMany") ||
               hasAnnotation(lsiField, "ManyToMany")
    }

    /**
     * 检测计算getter方法
     */
    private fun isComputedGetterMethod(lsiMethod: LsiMethod): Boolean {
        // 检查方法是否有@Formula注解
        val hasFormula = lsiMethod.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" ||
            annotation.simpleName == "Formula"
        }
        
        if (hasFormula) return true
        
        // 检查方法名是否符合getter模式且有计算逻辑
        val methodName = lsiMethod.name ?: return false
        
        return (methodName.startsWith("get") || methodName.startsWith("is")) &&
               hasComputedLogic(lsiMethod)
    }

    /**
     * 检查方法是否包含计算逻辑
     */
    private fun hasComputedLogic(lsiMethod: LsiMethod): Boolean {
        // 这里需要分析方法体来判断是否包含计算逻辑
        // 由于LSI可能不提供方法体分析，我们使用简单的启发式规则
        
        // 检查方法是否有依赖注解
        val hasDependencies = lsiMethod.annotations.any { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" &&
            annotation.getAttribute("dependencies") != null
        }
        
        return hasDependencies
    }

    /**
     * 确定计算属性类型
     */
    private fun determineComputedPropertyType(lsiField: LsiField): ComputedPropertyType {
        return when {
            isFormulaProperty(lsiField) -> ComputedPropertyType.FORMULA
            isIdViewProperty(lsiField) -> ComputedPropertyType.ID_VIEW
            isTransientProperty(lsiField) -> ComputedPropertyType.TRANSIENT
            else -> ComputedPropertyType.COMPUTED_GETTER
        }
    }

    /**
     * 提取Formula公式
     */
    private fun extractFormula(lsiField: LsiField): String? {
        val formulaAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" ||
            annotation.simpleName == "Formula"
        }
        
        return formulaAnnotation?.getAttribute("sql") as? String
    }

    /**
     * 提取IdView配置
     */
    private fun extractIdView(lsiField: LsiField): String? {
        val idViewAnnotation = lsiField.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.sql.IdView" ||
            annotation.simpleName == "IdView"
        }
        
        return idViewAnnotation?.getAttribute("value") as? String
    }

    /**
     * 从方法中提取Formula公式
     */
    private fun extractFormulaFromMethod(lsiMethod: LsiMethod): String? {
        val formulaAnnotation = lsiMethod.annotations.find { annotation ->
            annotation.qualifiedName == "org.babyfish.jimmer.Formula" ||
            annotation.simpleName == "Formula"
        }
        
        return formulaAnnotation?.getAttribute("sql") as? String
    }

    /**
     * 从getter方法名提取属性名
     */
    private fun extractPropertyNameFromGetter(lsiMethod: LsiMethod): String {
        val methodName = lsiMethod.name ?: return "unknown"
        
        return when {
            methodName.startsWith("get") -> {
                val propertyName = methodName.substring(3)
                propertyName.replaceFirstChar { it.lowercase() }
            }
            methodName.startsWith("is") -> {
                val propertyName = methodName.substring(2)
                propertyName.replaceFirstChar { it.lowercase() }
            }
            else -> methodName
        }
    }

    private fun hasAnnotation(lsiField: LsiField, annotationName: String): Boolean {
        return lsiField.annotations.any { annotation ->
            annotation.qualifiedName?.endsWith(".$annotationName") == true ||
            annotation.simpleName == annotationName
        }
    }

    /**
     * 计算属性类型枚举
     */
    enum class ComputedPropertyType {
        FORMULA,        // @Formula注解的计算属性
        ID_VIEW,        // @IdView注解的ID视图
        TRANSIENT,      // @Transient注解的临时属性
        COMPUTED_GETTER // 计算getter方法
    }

    /**
     * 计算属性信息
     */
    data class ComputedPropertyInfo(
        val name: String,
        val type: ComputedPropertyType,
        val formula: String?,
        val idView: String?,
        val isTransient: Boolean
    )
}