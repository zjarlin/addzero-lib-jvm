package site.addzero.processor.feign

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*

class ControllerMetadataExtractor(private val logger: KSPLogger) {

    fun extract(controller: KSClassDeclaration): ControllerMeta {
        val className = controller.simpleName.asString()
        val packageName = controller.packageName.asString()
        val basePath = extractBasePath(controller)
        val description = extractApiDescription(controller)
        
        val methods = controller.getAllFunctions()
            .filter { it.isPublic() && it.hasHttpAnnotation() }
            .mapNotNull { extractMethodMeta(it, basePath) }
            .toList()

        return ControllerMeta(className, packageName, basePath, methods, description)
    }

    private fun extractApiDescription(controller: KSClassDeclaration): String {
        val apiAnno = controller.annotations.find { 
            it.shortName.asString() == "Api" 
        } ?: return controller.docString?.trim() ?: ""
        
        val tagsArg = apiAnno.arguments.find { it.name?.asString() == "tags" }
        val valueArg = apiAnno.arguments.find { it.name?.asString() == "value" }
        
        return extractStringValue(tagsArg?.value) 
            ?: extractStringValue(valueArg?.value) 
            ?: controller.docString?.trim() 
            ?: ""
    }

    private fun extractApiOperationValue(function: KSFunctionDeclaration): String {
        val apiOpAnno = function.annotations.find { 
            it.shortName.asString() == "ApiOperation" 
        } ?: return function.docString?.trim() ?: ""
        
        val valueArg = apiOpAnno.arguments.find { it.name?.asString() == "value" }
        return extractStringValue(valueArg?.value) ?: function.docString?.trim() ?: ""
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractStringValue(value: Any?): String? {
        return when (value) {
            is String -> value.takeIf { it.isNotBlank() }
            is List<*> -> (value.firstOrNull() as? String)?.takeIf { it.isNotBlank() }
            else -> null
        }
    }

    private fun extractBasePath(controller: KSClassDeclaration): String {
        val requestMapping = controller.annotations.find { 
            it.shortName.asString() == "RequestMapping" 
        } ?: return ""
        return extractPathFromAnnotation(requestMapping)
    }

    private fun extractMethodMeta(function: KSFunctionDeclaration, basePath: String): MethodMeta? {
        val httpInfo = extractHttpInfo(function) ?: return null
        val fullPath = combinePath(basePath, httpInfo.second)
        val returnType = extractReturnType(function)
        val parameters = function.parameters.map { extractParamMeta(it) }
        val description = extractApiOperationValue(function)

        return MethodMeta(
            name = function.simpleName.asString(),
            httpMethod = httpInfo.first,
            path = fullPath,
            returnType = returnType,
            parameters = parameters,
            description = description
        )
    }

    private fun extractHttpInfo(function: KSFunctionDeclaration): Pair<HttpMethod, String>? {
        val mappingAnnotations = mapOf(
            "GetMapping" to HttpMethod.GET,
            "PostMapping" to HttpMethod.POST,
            "PutMapping" to HttpMethod.PUT,
            "DeleteMapping" to HttpMethod.DELETE,
            "PatchMapping" to HttpMethod.PATCH
        )

        for (annotation in function.annotations) {
            val annoName = annotation.shortName.asString()
            mappingAnnotations[annoName]?.let { httpMethod ->
                val path = extractPathFromAnnotation(annotation)
                return httpMethod to path
            }
            if (annoName == "RequestMapping") {
                val path = extractPathFromAnnotation(annotation)
                val methodArg = annotation.arguments.find { it.name?.asString() == "method" }
                val methodValue = methodArg?.value?.toString()?.substringAfterLast(".") ?: "GET"
                return HttpMethod.valueOf(methodValue) to path
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractPathFromAnnotation(annotation: KSAnnotation): String {
        val valueArg = annotation.arguments.find { 
            it.name?.asString() in listOf("value", "path") 
        } ?: return ""

        val value = valueArg.value
        return when (value) {
            is String -> value
            is List<*> -> (value as? List<*>)?.firstOrNull()?.toString() ?: ""
            else -> value.toString()
        }.removeSurrounding("\"").removeSurrounding("[", "]").trim()
    }

    private fun extractReturnType(function: KSFunctionDeclaration): String {
        val returnType = function.returnType?.resolve() ?: return "Unit"
        val qualifiedName = returnType.declaration.qualifiedName?.asString() ?: "Any"

        // ResponseEntity<T> -> T
        if (qualifiedName == "org.springframework.http.ResponseEntity") {
            val typeArg = returnType.arguments.firstOrNull()?.type?.resolve()
            return typeArg?.declaration?.qualifiedName?.asString() ?: "Any"
        }

        // 处理泛型
        return if (returnType.arguments.isNotEmpty()) {
            val baseType = qualifiedName
            val typeArgs = returnType.arguments.joinToString(", ") { arg ->
                arg.type?.resolve()?.declaration?.qualifiedName?.asString() ?: "*"
            }
            "$baseType<$typeArgs>"
        } else {
            qualifiedName
        }
    }

    private fun extractParamMeta(param: KSValueParameter): ParamMeta {
        val name = param.name?.asString() ?: ""
        val type = param.type.resolve().let { 
            val qualifiedName = it.declaration.qualifiedName?.asString() ?: "Any"
            if (it.arguments.isNotEmpty()) {
                val typeArgs = it.arguments.joinToString(", ") { arg ->
                    arg.type?.resolve()?.declaration?.qualifiedName?.asString() ?: "*"
                }
                "$qualifiedName<$typeArgs>"
            } else {
                qualifiedName
            }
        }

        val annotation = param.annotations.mapNotNull { anno ->
            when (anno.shortName.asString()) {
                "PathVariable" -> ParamAnnotation.PATH_VARIABLE
                "RequestParam" -> ParamAnnotation.REQUEST_PARAM
                "RequestBody" -> ParamAnnotation.REQUEST_BODY
                "RequestHeader" -> ParamAnnotation.REQUEST_HEADER
                else -> null
            }
        }.firstOrNull() ?: ParamAnnotation.REQUEST_PARAM

        return ParamMeta(name, type, annotation)
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

    private fun KSFunctionDeclaration.isPublic(): Boolean {
        return !modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.PROTECTED)
    }

    private fun KSFunctionDeclaration.hasHttpAnnotation(): Boolean {
        val httpAnnotations = setOf(
            "GetMapping", "PostMapping", "PutMapping", 
            "DeleteMapping", "PatchMapping", "RequestMapping"
        )
        return annotations.any { it.shortName.asString() in httpAnnotations }
    }
}
