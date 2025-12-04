package site.addzero.apt.feign

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi.method.LsiParameter

object ControllerMetadataExtractor {

    private val HTTP_MAPPING_ANNOTATIONS = mapOf(
        "GetMapping" to HttpMethod.GET,
        "PostMapping" to HttpMethod.POST,
        "PutMapping" to HttpMethod.PUT,
        "DeleteMapping" to HttpMethod.DELETE,
        "PatchMapping" to HttpMethod.PATCH
    )

    private val PARAM_ANNOTATION_MAP = mapOf(
        "PathVariable" to ParamAnnotation.PATH_VARIABLE,
        "RequestParam" to ParamAnnotation.REQUEST_PARAM,
        "RequestBody" to ParamAnnotation.REQUEST_BODY,
        "RequestHeader" to ParamAnnotation.REQUEST_HEADER
    )

    fun extract(lsiClass: LsiClass): ControllerMeta {
        val className = lsiClass.name ?: ""
        val packageName = lsiClass.qualifiedName?.substringBeforeLast('.') ?: ""
        val basePath = lsiClass.annotations.extractPath("RequestMapping")
        
        val methods = lsiClass.methods
            .filter { it.hasHttpAnnotation() }
            .mapNotNull { extractMethodMeta(it, basePath) }

        return ControllerMeta(className, packageName, basePath, lsiClass.comment, methods)
    }

    private fun extractMethodMeta(method: LsiMethod, basePath: String): MethodMeta? {
        val httpInfo = method.extractHttpInfo() ?: return null
        val fullPath = combinePath(basePath, httpInfo.second)
        val returnType = method.extractReturnType()
        val parameters = method.parameters.map { it.extractParamMeta() }

        return MethodMeta(
            name = method.name ?: "",
            httpMethod = httpInfo.first,
            path = fullPath,
            returnType = returnType,
            comment = method.comment,
            parameters = parameters
        )
    }

    private fun LsiMethod.hasHttpAnnotation(): Boolean =
        annotations.any { it.simpleName in HTTP_MAPPING_ANNOTATIONS.keys || it.simpleName == "RequestMapping" }

    private fun LsiMethod.extractHttpInfo(): Pair<HttpMethod, String>? {
        for (anno in annotations) {
            HTTP_MAPPING_ANNOTATIONS[anno.simpleName]?.let { httpMethod ->
                return httpMethod to anno.extractPath()
            }
            if (anno.simpleName == "RequestMapping") {
                val path = anno.extractPath()
                val methodAttr = anno.getAttribute("method")?.toString()?.substringAfterLast(".") ?: "GET"
                return HttpMethod.valueOf(methodAttr) to path
            }
        }
        return null
    }

    private fun LsiMethod.extractReturnType(): String {
        val typeName = returnTypeName ?: "Object"
        return if (typeName.startsWith("org.springframework.http.ResponseEntity")) {
            returnType?.typeParameters?.firstOrNull()?.qualifiedName ?: "Object"
        } else typeName
    }

    private fun LsiParameter.extractParamMeta(): ParamMeta {
        val annotation = annotations.firstNotNullOfOrNull { PARAM_ANNOTATION_MAP[it.simpleName] }
            ?: ParamAnnotation.REQUEST_PARAM
        return ParamMeta(name ?: "", typeName ?: "Object", annotation)
    }

    private fun List<LsiAnnotation>.extractPath(annotationName: String): String =
        find { it.simpleName == annotationName }?.extractPath() ?: ""

    private fun LsiAnnotation.extractPath(): String {
        val value = getAttribute("value") ?: getAttribute("path") ?: return ""
        return when (value) {
            is String -> value
            is List<*> -> value.firstOrNull()?.toString() ?: ""
            else -> value.toString()
        }.removeSurrounding("\"")
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
