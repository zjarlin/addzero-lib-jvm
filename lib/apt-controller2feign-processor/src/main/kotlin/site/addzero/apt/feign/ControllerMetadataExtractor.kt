package site.addzero.apt.feign

import site.addzero.util.lsi_impl.impl.apt.anno.classComment
import site.addzero.util.lsi_impl.impl.apt.anno.methodComment
import site.addzero.util.str.firstNotBlank
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

class ControllerMetadataExtractor(private val processingEnv: ProcessingEnvironment) {

    private val elementUtils = processingEnv.elementUtils

    fun extract(controllerElement: TypeElement): ControllerMeta {
        val className = controllerElement.simpleName.toString()
        val packageName = elementUtils.getPackageOf(controllerElement).qualifiedName.toString()
        val basePath = extractBasePath(controllerElement)
        val docComment = elementUtils.getDocComment(controllerElement)
        val classComment = firstNotBlank(docComment, controllerElement.annotationMirrors.classComment())
        
        val methods = controllerElement.enclosedElements
            .filterIsInstance<ExecutableElement>()
            .filter { it.kind == ElementKind.METHOD && hasHttpAnnotation(it) }
            .mapNotNull { extractMethodMeta(it, basePath) }

        return ControllerMeta(className, packageName, basePath, classComment, methods)
    }

    private fun extractBasePath(element: TypeElement): String {
        val requestMapping = element.annotationMirrors.find { 
            it.annotationType.toString().endsWith("RequestMapping") 
        } ?: return ""
        return extractPathFromAnnotation(requestMapping)
    }

    private fun extractMethodMeta(method: ExecutableElement, basePath: String): MethodMeta? {
        val httpInfo = extractHttpInfo(method) ?: return null
        val fullPath = combinePath(basePath, httpInfo.second)
        val returnType = extractReturnType(method.returnType)
        val parameters = method.parameters.map { extractParamMeta(it) }
        
        val docComment = elementUtils.getDocComment(method)
        val methodComment = firstNotBlank(docComment, method.annotationMirrors.methodComment())

        return MethodMeta(
            name = method.simpleName.toString(),
            httpMethod = httpInfo.first,
            path = fullPath,
            returnType = returnType,
            comment = methodComment,
            parameters = parameters
        )
    }

    private fun extractHttpInfo(method: ExecutableElement): Pair<HttpMethod, String>? {
        val mappingAnnotations = mapOf(
            "GetMapping" to HttpMethod.GET,
            "PostMapping" to HttpMethod.POST,
            "PutMapping" to HttpMethod.PUT,
            "DeleteMapping" to HttpMethod.DELETE,
            "PatchMapping" to HttpMethod.PATCH
        )

        for (annotation in method.annotationMirrors) {
            val annotationName = annotation.annotationType.asElement().simpleName.toString()
            mappingAnnotations[annotationName]?.let { httpMethod ->
                val path = extractPathFromAnnotation(annotation)
                return httpMethod to path
            }
            if (annotationName == "RequestMapping") {
                val path = extractPathFromAnnotation(annotation)
                val methodAttr = annotation.elementValues.entries.find { 
                    it.key.simpleName.toString() == "method" 
                }?.value?.value?.toString()?.substringAfterLast(".") ?: "GET"
                return HttpMethod.valueOf(methodAttr) to path
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractPathFromAnnotation(annotation: AnnotationMirror): String {
        val valueEntry = annotation.elementValues.entries.find { entry ->
            entry.key.simpleName.toString() in listOf("value", "path")
        } ?: return ""

        val value = valueEntry.value.value
        return when (value) {
            is String -> value
            is List<*> -> (value as? List<AnnotationValue>)?.firstOrNull()?.value?.toString() ?: ""
            else -> value.toString()
        }.removeSurrounding("\"")
    }

    private fun extractReturnType(typeMirror: TypeMirror): String {
        return when {
            typeMirror.toString().startsWith("org.springframework.http.ResponseEntity") -> {
                val declaredType = typeMirror as? DeclaredType
                declaredType?.typeArguments?.firstOrNull()?.toString() ?: "Object"
            }
            else -> typeMirror.toString()
        }
    }

    private fun extractParamMeta(param: VariableElement): ParamMeta {
        val name = param.simpleName.toString()
        val type = param.asType().toString()
        val annotation = param.annotationMirrors.mapNotNull { anno ->
            when (anno.annotationType.asElement().simpleName.toString()) {
                "PathVariable" -> ParamAnnotation.PATH_VARIABLE
                "RequestParam" -> ParamAnnotation.REQUEST_PARAM
                "RequestBody" -> ParamAnnotation.REQUEST_BODY
                "RequestHeader" -> ParamAnnotation.REQUEST_HEADER
                else -> null
            }
        }.firstOrNull() ?: ParamAnnotation.REQUEST_PARAM

        return ParamMeta(name, type, annotation)
    }

    private fun hasHttpAnnotation(method: ExecutableElement): Boolean {
        val httpAnnotations = setOf(
            "GetMapping", "PostMapping", "PutMapping", 
            "DeleteMapping", "PatchMapping", "RequestMapping"
        )
        return method.annotationMirrors.any { 
            it.annotationType.asElement().simpleName.toString() in httpAnnotations 
        }
    }

    private fun combinePath(basePath: String, methodPath: String): String {
        val base = basePath.trimEnd('/')
        val method = methodPath.trimStart('/')
        return when {
            base.isEmpty() -> "/$method"
            method.isEmpty() -> base
            else -> "$base/$method"
        }
    }
}
