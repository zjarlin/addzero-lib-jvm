import io.swagger.annotations.ApiModelProperty
import site.addzero.util.metainfo.MetaInfoUtils
import site.addzero.util.FieldDTO
import java.lang.reflect.AnnotatedElement

fun main() {
    println("Testing MetaInfoUtils with FieldDTO")
    
    // 检查Swagger注解类是否可以加载
    try {
        val clazz = Class.forName("io.swagger.annotations.ApiModelProperty")
        println("Successfully loaded ApiModelProperty class: $clazz")
    } catch (e: Exception) {
        println("Failed to load ApiModelProperty class: ${e.message}")
    }
    
    // 检查FieldDTO类
    val fieldDTOClass = FieldDTO::class.java
    println("FieldDTO class: $fieldDTOClass")
    
    // 获取restName字段
    val restNameField = fieldDTOClass.getDeclaredField("restName")
    println("restName field: $restNameField")
    
    // 检查字段上的注解
    val annotations = restNameField.annotations
    println("Annotations on restName field: ${annotations.contentToString()}")
    
    // 检查是否有ApiModelProperty注解
    val hasAnnotation = restNameField.isAnnotationPresent(ApiModelProperty::class.java)
    println("Has ApiModelProperty annotation: $hasAnnotation")
    
    // 使用MetaInfoUtils.guessDescription
    val description = MetaInfoUtils.guessDescription(restNameField)
    println("Guessed description: $description")
}