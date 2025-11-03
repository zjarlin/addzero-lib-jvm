package site.addzero.util

import cn.hutool.core.util.ClassUtil
import io.swagger.annotations.ApiOperation
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.bind.annotation.RequestMapping
import site.addzero.util.metainfo.MetaInfoUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object AbsFunBox {
    private const val STRING_LENGTH_DEFAULT = 255

    /**
     * 获取元素的 RequestMapping 注解信息（请求方法 + 路径）
     */
    private fun getRequestMapping(element: AnnotatedElement): Pair<String, String> {
        val annotation = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping::class.java) ?: return "" to ""
        // 提取请求方法（默认取第一个，无则空）
        val methodType = annotation.method.firstOrNull()?.name ?: ""
        // 提取请求路径（默认取第一个，无则空）
        val requestPath = annotation.value.firstOrNull().orEmpty()
        return methodType to requestPath
    }

    /**
     * 扫描指定包下的 Controller，获取所有接口方法信息
     */
    fun getAllFun(controllerPackageName: String?): List<FunBox> {
        // 扫描包下所有带 RequestMapping 注解的类（排除自身）
        val controllerClasses = ClassUtil.scanPackage(controllerPackageName) { clazz ->
            AnnotatedElementUtils.hasAnnotation(clazz, RequestMapping::class.java)
                    && clazz.name != AbsFunBox::class.java.name
        }
        return getAllFun(controllerClasses)
    }

    /**
     * 从 Controller 类集合中提取所有接口方法信息
     */
    fun getAllFun(classes: Set<Class<*>>): List<FunBox> {
        return classes.flatMap { controllerClass ->
            // 获取 Controller 类的 RequestMapping 路径
            val (_, controllerPath) = getRequestMapping(controllerClass)
            // 反射获取 Controller 中带 RequestMapping 的非静态方法
            val apiMethods = controllerClass.declaredMethods.filter { method ->
                AnnotatedElementUtils.hasAnnotation(method, RequestMapping::class.java)
                        && !Modifier.isStatic(method.modifiers)
            }
            // 转换每个方法为 FunBox
            apiMethods.map { method ->
                val (methodType, methodPath) = getRequestMapping(method)
                // 拼接完整接口路径（确保前缀带 /）
                val fullPath = buildString {
                    if (controllerPath.isNotBlank()) append("/${controllerPath.removePrefix("/")}")
                    if (methodPath.isNotBlank()) append("/${methodPath.removePrefix("/")}")
                }.takeIf { it.isNotBlank() } ?: ""

                FunBox().apply {
                    restUrl = fullPath
                    this.methodType = methodType
                    des = method.getAnnotation(ApiOperation::class.java)?.value.orEmpty()
                    funName = method.name
                    paramiter = getParamsDTO(methodType, method)
                    returns = getReturnDTO(method.returnType)
                }
            }
        }
    }

    /**
     * 生成方法返回值的字段信息
     */
    private fun getReturnDTO(returnType: Class<*>): MutableList<FieldDTO> {
        return genFieldDTO(returnType)
    }

    /**
     * 根据请求方法类型（GET/POST）生成参数字段信息
     */
    private fun getParamsDTO(methodType: String, method: Method): MutableList<FieldDTO> {
        return mutableListOf<FieldDTO>().apply {
            when {
                methodType.equals("GET", ignoreCase = true) -> {
                    // GET 请求：直接解析方法参数
                    method.parameters.forEach { param ->
                        val desc = MetaInfoUtils.guessDescription(param)
                        add(
                            FieldDTO().apply {

                                fieldName = desc?.ifBlank { param.name }
                                fieldEng = param.name.orEmpty()
                                fieldType = param.type.simpleName
                                fieldLong = if (param.type == String::class.java) STRING_LENGTH_DEFAULT.toString() else "0"
                            }
                        )
                    }
                }

                methodType.equals("POST", ignoreCase = true) -> {
                    // POST 请求：解析参数类型的成员字段
                    method.parameters.forEach { param ->
                        val paramClass = param.type
                        addAll(genFieldDTO(paramClass))
                    }
                }
            }
        }
    }

    /**
     * 递归生成类的字段信息（处理嵌套类型）
     */
    private fun genFieldDTO(targetClass: Class<*>): MutableList<FieldDTO> {
        return mutableListOf<FieldDTO>().apply {
            // 获取类的非静态字段
            val fields = targetClass.declaredFields.filter { !Modifier.isStatic(it.modifiers) }

            fields.forEach { field ->
                // 检查是否为需要特殊处理的类型（原 RefUtil.isT 逻辑，此处保留调用）
                if (RefUtil.isT(field)) {
                    // 递归处理嵌套类型
                    addAll(genFieldDTO(field.type))
                    return@forEach
                }

                // 提取字段描述和名称
                val fieldDesc = MetaInfoUtils.guessDescription(field).orEmpty()
                val fieldName = fieldDesc.ifBlank { field.name }

                add(
                    FieldDTO().apply {
                        this.fieldName = fieldName
                        fieldEng = field.name
                        fieldType = field.type.simpleName
                        // 字符串类型默认长度 255，其他类型 0
                        fieldLong = if (field.type == String::class.java) STRING_LENGTH_DEFAULT.toString() else "0"
                    }
                )
            }
        }
    }

}
