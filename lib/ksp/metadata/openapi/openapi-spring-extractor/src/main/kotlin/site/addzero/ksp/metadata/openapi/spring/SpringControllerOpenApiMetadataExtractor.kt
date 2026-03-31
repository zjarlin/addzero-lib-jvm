package site.addzero.ksp.metadata.openapi.spring

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.validate
import site.addzero.ksp.metadata.openapi.model.OpenApiEndpointMeta
import site.addzero.ksp.metadata.openapi.model.OpenApiExtractionWarning
import site.addzero.ksp.metadata.openapi.model.OpenApiFramework
import site.addzero.ksp.metadata.openapi.model.OpenApiHttpMethod
import site.addzero.ksp.metadata.openapi.model.OpenApiMetadataBundle
import site.addzero.ksp.metadata.openapi.model.OpenApiOwnerKind
import site.addzero.ksp.metadata.openapi.model.OpenApiOwnerMeta
import site.addzero.ksp.metadata.openapi.model.OpenApiParameterLocation
import site.addzero.ksp.metadata.openapi.model.OpenApiParameterMeta
import site.addzero.ksp.metadata.openapi.model.OpenApiRequestBodyMeta
import site.addzero.ksp.metadata.openapi.model.OpenApiResponseBodyMeta
import site.addzero.ksp.metadata.openapi.model.OpenApiTypeMeta

private const val REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController"
private const val REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping"
private const val FILE_REQUEST_MAPPING = "site.addzero.springktor.runtime.RequestMapping"
private const val GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"
private const val POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping"
private const val PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping"
private const val DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping"
private const val PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping"
private const val PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable"
private const val REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam"
private const val REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody"
private const val REQUEST_HEADER = "org.springframework.web.bind.annotation.RequestHeader"
private const val REQUEST_PART = "org.springframework.web.bind.annotation.RequestPart"
private const val RESPONSE_ENTITY = "org.springframework.http.ResponseEntity"
private const val HTTP_ENTITY = "org.springframework.http.HttpEntity"

private const val APPLICATION_CALL = "io.ktor.server.application.ApplicationCall"
private const val APPLICATION = "io.ktor.server.application.Application"
private const val ROUTING_CONTEXT = "io.ktor.server.routing.RoutingContext"
private const val APPLICATION_REQUEST = "io.ktor.server.request.ApplicationRequest"
private const val APPLICATION_RESPONSE = "io.ktor.server.response.ApplicationResponse"
private const val MULTIPART_FILE = "org.springframework.web.multipart.MultipartFile"
private const val LIST = "kotlin.collections.List"
private const val SET = "kotlin.collections.Set"
private const val COLLECTION = "kotlin.collections.Collection"
private const val MAP = "kotlin.collections.Map"
private const val STRING = "kotlin.String"
private const val BYTE_ARRAY = "kotlin.ByteArray"
private const val UNIT = "kotlin.Unit"

private const val SWAGGER_API = "io.swagger.annotations.Api"
private const val SWAGGER_API_OPERATION = "io.swagger.annotations.ApiOperation"
private const val OAS_OPERATION = "io.swagger.v3.oas.annotations.Operation"
private const val OAS_TAG = "io.swagger.v3.oas.annotations.tags.Tag"

private val mappingAnnotations = linkedMapOf(
    GET_MAPPING to OpenApiHttpMethod.GET,
    POST_MAPPING to OpenApiHttpMethod.POST,
    PUT_MAPPING to OpenApiHttpMethod.PUT,
    DELETE_MAPPING to OpenApiHttpMethod.DELETE,
    PATCH_MAPPING to OpenApiHttpMethod.PATCH,
    REQUEST_MAPPING to null,
)

private val simpleBindableTypes = setOf(
    "kotlin.String",
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Short",
    "kotlin.Byte",
    "kotlin.Double",
    "kotlin.Float",
    "kotlin.Boolean",
    "kotlin.Char",
    "java.math.BigDecimal",
    "java.math.BigInteger",
    "java.util.UUID",
)

data class SpringControllerOpenApiExtractionResult(
    val metadata: OpenApiMetadataBundle,
    val deferred: List<KSAnnotated>,
)

object SpringControllerOpenApiMetadata {
    fun extract(
        resolver: Resolver,
        logger: KSPLogger? = null,
    ): SpringControllerOpenApiExtractionResult {
        return SpringControllerOpenApiMetadataExtractor(resolver, logger).extract()
    }
}

class SpringControllerOpenApiMetadataExtractor(
    private val resolver: Resolver,
    private val logger: KSPLogger? = null,
) {
    private val deferred = linkedSetOf<KSAnnotated>()
    private val warnings = mutableListOf<OpenApiExtractionWarning>()
    private val endpoints = linkedMapOf<String, OpenApiEndpointMeta>()

    fun extract(): SpringControllerOpenApiExtractionResult {
        val visited = linkedSetOf<String>()
        for ((annotationName, _) in mappingAnnotations) {
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
                .filterIsInstance<KSFunctionDeclaration>()
            for (function in symbols) {
                if (!function.validate()) {
                    deferred += function
                    continue
                }
                val signature = function.signatureKey()
                if (!visited.add(signature)) {
                    continue
                }
                extractEndpoint(function)?.let { endpoints[signature] = it }
            }
        }
        validateCollisions()
        return SpringControllerOpenApiExtractionResult(
            metadata = OpenApiMetadataBundle(
                framework = OpenApiFramework.SPRING,
                endpoints = endpoints.values.sortedWith(compareBy({ it.path }, { it.httpMethod?.ordinal ?: Int.MAX_VALUE }, { it.operationId })),
                warnings = warnings.toList(),
            ),
            deferred = deferred.toList(),
        )
    }

    private fun extractEndpoint(function: KSFunctionDeclaration): OpenApiEndpointMeta? {
        if (function.extensionReceiver != null || !function.isAccessible()) {
            return null
        }
        val mapping = extractHttpMapping(function) ?: return null
        val owner = buildOwner(function) ?: return null
        val docs = extractDocs(function)
        val mediaTypes = extractMediaTypes(function)
        val parameterExtraction = extractParameters(function, mediaTypes.consumes)
        val responseBody = extractResponseBody(function, mediaTypes.produces)
        return OpenApiEndpointMeta(
            framework = OpenApiFramework.SPRING,
            owner = owner,
            httpMethod = mapping.method,
            path = mapping.path,
            functionName = function.simpleName.asString(),
            operationId = buildOperationId(function, mapping.method),
            summary = docs.first,
            description = docs.second,
            tags = extractTags(function, owner),
            consumes = mediaTypes.consumes,
            produces = mediaTypes.produces,
            parameters = parameterExtraction.parameters,
            requestBody = parameterExtraction.requestBody,
            responseBody = responseBody,
            isSuspend = Modifier.SUSPEND in function.modifiers,
        )
    }

    private fun buildOwner(function: KSFunctionDeclaration): OpenApiOwnerMeta? {
        val parent = function.parentDeclaration
        if (parent == null) {
            return OpenApiOwnerMeta(
                kind = OpenApiOwnerKind.FILE,
                packageName = function.packageName.asString(),
                simpleName = function.containingFile?.fileName?.removeSuffix(".kt") ?: function.simpleName.asString(),
                qualifiedName = function.qualifiedName?.asString() ?: "${function.packageName.asString()}.${function.simpleName.asString()}",
                sourceFile = function.containingFile?.filePath,
            )
        }
        val controller = parent as? KSClassDeclaration ?: return null
        if (!controller.hasAnnotation(REST_CONTROLLER)) {
            warn("Spring endpoint `${function.simpleName.asString()}` is ignored because its parent is not `@RestController`.", function)
            return null
        }
        return OpenApiOwnerMeta(
            kind = OpenApiOwnerKind.CONTROLLER,
            packageName = controller.packageName.asString(),
            simpleName = controller.simpleName.asString(),
            qualifiedName = controller.qualifiedName?.asString(),
            sourceFile = controller.containingFile?.filePath,
        )
    }

    private fun extractHttpMapping(function: KSFunctionDeclaration): HttpMapping? {
        for ((annotationName, fixedMethod) in mappingAnnotations) {
            val annotation = function.findAnnotation(annotationName) ?: continue
            val path = combinePaths(extractBasePath(function), extractMappingPath(annotation))
            if (fixedMethod != null) {
                return HttpMapping(fixedMethod, path)
            }
            val methods = extractRequestMethods(annotation)
            if (methods.size > 1) {
                warn("Only the first Spring `@RequestMapping` method is used on `${function.simpleName.asString()}`.", function)
            }
            return HttpMapping(methods.firstOrNull() ?: OpenApiHttpMethod.GET, path)
        }
        return null
    }

    private fun extractBasePath(function: KSFunctionDeclaration): String {
        val controller = function.parentDeclaration as? KSClassDeclaration
        if (controller != null) {
            return controller.findAnnotation(REQUEST_MAPPING)?.let(::extractMappingPath).orEmpty()
        }
        return function.containingFile?.findFileAnnotation(FILE_REQUEST_MAPPING)?.let(::extractMappingPath).orEmpty()
    }

    private fun extractParameters(
        function: KSFunctionDeclaration,
        consumes: List<String>,
    ): ParameterExtraction {
        val parameters = mutableListOf<OpenApiParameterMeta>()
        val parts = mutableListOf<OpenApiParameterMeta>()
        var requestBody: OpenApiRequestBodyMeta? = null
        for ((index, parameter) in function.parameters.withIndex()) {
            val parameterName = parameter.name?.asString() ?: continue
            val resolvedType = parameter.type.resolve()
            val binding = determineBinding(parameter, resolvedType)
            if (binding.kind == BindingKind.CONTEXT) {
                continue
            }
            val typeMeta = resolvedType.toTypeMeta()
            val meta = OpenApiParameterMeta(
                index = index,
                sourceName = parameterName,
                externalName = binding.externalName,
                location = binding.location,
                type = typeMeta,
                required = binding.required,
            )
            when (binding.kind) {
                BindingKind.PATH,
                BindingKind.QUERY,
                BindingKind.HEADER,
                BindingKind.UNKNOWN -> {
                    parameters += meta
                    if (binding.kind == BindingKind.UNKNOWN) {
                        warn("Spring parameter `$parameterName` on `${function.simpleName.asString()}` was marked as UNKNOWN.", parameter)
                    }
                }

                BindingKind.BODY -> {
                    requestBody = OpenApiRequestBodyMeta(
                        type = typeMeta,
                        required = binding.required,
                        contentTypes = consumes.ifEmpty { listOf("application/json") },
                    )
                }

                BindingKind.PART -> {
                    parts += meta.copy(location = OpenApiParameterLocation.PART)
                }

                BindingKind.CONTEXT -> {
                }
            }
        }
        if (parts.isNotEmpty()) {
            requestBody = (requestBody ?: OpenApiRequestBodyMeta()).copy(
                required = requestBody?.required ?: parts.any { it.required },
                contentTypes = requestBody?.contentTypes?.ifEmpty { consumes.ifEmpty { listOf("multipart/form-data") } }
                    ?: consumes.ifEmpty { listOf("multipart/form-data") },
                parts = parts.toList(),
            )
        }
        return ParameterExtraction(parameters, requestBody)
    }

    private fun determineBinding(parameter: KSValueParameter, type: KSType): ParameterBinding {
        val name = parameter.name?.asString().orEmpty()
        val nullable = type.nullability == Nullability.NULLABLE || parameter.hasDefault
        val requiredByDefault = !nullable
        val typeName = type.declaration.qualifiedName?.asString()
        if (typeName in setOf(APPLICATION_CALL, APPLICATION, ROUTING_CONTEXT, APPLICATION_REQUEST, APPLICATION_RESPONSE)) {
            return ParameterBinding(BindingKind.CONTEXT, OpenApiParameterLocation.CONTEXT, name, false)
        }
        parameter.findAnnotation(PATH_VARIABLE)?.let {
            return ParameterBinding(BindingKind.PATH, OpenApiParameterLocation.PATH, extractNamedValue(it, name), extractRequired(it, true) && requiredByDefault)
        }
        parameter.findAnnotation(REQUEST_PARAM)?.let {
            return ParameterBinding(BindingKind.QUERY, OpenApiParameterLocation.QUERY, extractNamedValue(it, name), extractRequired(it, true) && requiredByDefault && !hasDefaultValue(it))
        }
        parameter.findAnnotation(REQUEST_BODY)?.let {
            return ParameterBinding(BindingKind.BODY, OpenApiParameterLocation.BODY, name, extractRequired(it, true) && requiredByDefault)
        }
        parameter.findAnnotation(REQUEST_HEADER)?.let {
            return ParameterBinding(BindingKind.HEADER, OpenApiParameterLocation.HEADER, extractNamedValue(it, name), extractRequired(it, true) && requiredByDefault && !hasDefaultValue(it))
        }
        parameter.findAnnotation(REQUEST_PART)?.let {
            return ParameterBinding(BindingKind.PART, OpenApiParameterLocation.PART, extractNamedValue(it, name), extractRequired(it, true) && requiredByDefault)
        }
        if (type.isMultipartFileType() || type.isMultipartFileListType()) {
            return ParameterBinding(BindingKind.PART, OpenApiParameterLocation.PART, name, requiredByDefault)
        }
        if (type.isSimpleBindableType()) {
            return ParameterBinding(BindingKind.QUERY, OpenApiParameterLocation.QUERY, name, requiredByDefault)
        }
        return ParameterBinding(BindingKind.UNKNOWN, OpenApiParameterLocation.UNKNOWN, name, requiredByDefault)
    }

    private fun extractResponseBody(function: KSFunctionDeclaration, produces: List<String>): OpenApiResponseBodyMeta? {
        val resolved = function.returnType?.resolve()?.unwrapResponseEntity() ?: return null
        if (resolved.isUnitType()) {
            return null
        }
        return OpenApiResponseBodyMeta(
            type = resolved.toTypeMeta(),
            contentTypes = produces.ifEmpty { defaultProducesFor(resolved) },
        )
    }

    private fun defaultProducesFor(type: KSType): List<String> {
        return when (type.makeNotNullable().declaration.qualifiedName?.asString()) {
            STRING -> listOf("text/plain")
            BYTE_ARRAY -> listOf("application/octet-stream")
            else -> listOf("application/json")
        }
    }

    private fun extractMediaTypes(function: KSFunctionDeclaration): MediaTypes {
        val methodAnnotation = mappingAnnotations.keys.asSequence().mapNotNull { function.findAnnotation(it) }.firstOrNull()
        val controller = function.parentDeclaration as? KSClassDeclaration
        val classAnnotation = controller?.findAnnotation(REQUEST_MAPPING)
        return MediaTypes(
            consumes = extractStringValues(methodAnnotation?.argument("consumes")).ifEmpty { extractStringValues(classAnnotation?.argument("consumes")) }.distinct(),
            produces = extractStringValues(methodAnnotation?.argument("produces")).ifEmpty { extractStringValues(classAnnotation?.argument("produces")) }.distinct(),
        )
    }

    private fun extractDocs(function: KSFunctionDeclaration): Pair<String?, String?> {
        val docLines = function.docString?.lineSequence()?.map { it.trim() }?.filter { it.isNotBlank() }?.toList().orEmpty()
        val summary = extractFirstStringValue(function.findAnnotation(OAS_OPERATION)?.argument("summary"))
            ?: extractFirstStringValue(function.findAnnotation(SWAGGER_API_OPERATION)?.argument("value"))
            ?: docLines.firstOrNull()
        val description = extractFirstStringValue(function.findAnnotation(OAS_OPERATION)?.argument("description"))
            ?: extractFirstStringValue(function.findAnnotation(SWAGGER_API_OPERATION)?.argument("notes"))
            ?: docLines.joinToString("\n").ifBlank { null }
        return summary to description
    }

    private fun extractTags(function: KSFunctionDeclaration, owner: OpenApiOwnerMeta): List<String> {
        val methodTags = function.extractDeclaredTags()
        if (methodTags.isNotEmpty()) {
            return methodTags
        }
        val controllerTags = (function.parentDeclaration as? KSClassDeclaration)?.extractDeclaredTags().orEmpty()
        if (controllerTags.isNotEmpty()) {
            return controllerTags
        }
        return if (owner.kind == OpenApiOwnerKind.CONTROLLER) {
            listOf(owner.simpleName.removeSuffix("Controller").ifBlank { owner.simpleName })
        } else {
            listOf(owner.simpleName)
        }
    }

    private fun KSAnnotated.extractDeclaredTags(): List<String> {
        val oasTags = annotations
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == OAS_TAG }
            .mapNotNull { extractFirstStringValue(it.argument("name")) }
            .toList()
        if (oasTags.isNotEmpty()) {
            return oasTags.distinct()
        }
        val swaggerApi = findAnnotation(SWAGGER_API) ?: return emptyList()
        return extractStringValues(swaggerApi.argument("tags"))
            .ifEmpty { extractStringValues(swaggerApi.argument("value")) }
            .distinct()
    }

    private fun extractMappingPath(annotation: KSAnnotation): String {
        return normalizePath(
            extractStringValues(annotation.argument("path")).firstOrNull { it.isNotBlank() }
                ?: extractStringValues(annotation.argument("value")).firstOrNull { it.isNotBlank() }
                ?: "",
        )
    }

    private fun extractRequestMethods(annotation: KSAnnotation): List<OpenApiHttpMethod> {
        val values = when (val raw = annotation.argument("method")?.value) {
            is List<*> -> raw.mapNotNull { it?.toString() }
            null -> emptyList()
            else -> listOf(raw.toString())
        }
        return values.mapNotNull { runCatching { OpenApiHttpMethod.valueOf(it.substringAfterLast('.')) }.getOrNull() }
    }

    private fun validateCollisions() {
        val signatures = linkedMapOf<String, MutableList<String>>()
        endpoints.values.forEach { endpoint ->
            signatures.getOrPut("${endpoint.httpMethod}:${endpoint.path}") { mutableListOf() }.add(endpoint.operationId)
        }
        signatures.forEach { (signature, operationIds) ->
            if (operationIds.size > 1) {
                warn("Duplicate Spring route mapping `$signature`: ${operationIds.joinToString()}.")
            }
        }
    }

    private fun extractNamedValue(annotation: KSAnnotation, fallback: String): String {
        return extractStringValues(annotation.argument("name"))
            .ifEmpty { extractStringValues(annotation.argument("value")) }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
            .ifBlank { fallback }
    }

    private fun extractRequired(annotation: KSAnnotation, defaultValue: Boolean): Boolean {
        return annotation.argument("required")?.value as? Boolean ?: defaultValue
    }

    private fun hasDefaultValue(annotation: KSAnnotation): Boolean {
        val value = extractFirstStringValue(annotation.argument("defaultValue")).orEmpty()
        return value.isNotBlank() && value != "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n"
    }

    private fun extractFirstStringValue(argument: KSValueArgument?): String? {
        return extractStringValues(argument).firstOrNull { it.isNotBlank() }
    }

    private fun extractStringValues(argument: KSValueArgument?): List<String> {
        return when (val raw = argument?.value) {
            is String -> listOf(raw)
            is List<*> -> raw.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    private fun combinePaths(base: String, child: String): String {
        val normalizedBase = normalizePath(base)
        val normalizedChild = normalizePath(child)
        if (normalizedBase == "/" && normalizedChild == "/") return "/"
        if (normalizedBase == "/") return normalizedChild
        if (normalizedChild == "/") return normalizedBase
        return (normalizedBase.trimEnd('/') + "/" + normalizedChild.trimStart('/')).replace("//", "/")
    }

    private fun normalizePath(path: String): String {
        val trimmed = path.trim()
        if (trimmed.isBlank()) return "/"
        return (if (trimmed.startsWith('/')) trimmed else "/$trimmed").replace("//", "/")
    }

    private fun buildOperationId(function: KSFunctionDeclaration, method: OpenApiHttpMethod): String {
        val base = function.qualifiedName?.asString() ?: "${function.packageName.asString()}.${function.simpleName.asString()}"
        return "$base.${method.name.lowercase()}"
    }

    private fun KSValueParameter.findAnnotation(annotationName: String): KSAnnotation? = annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
    }

    private fun KSAnnotated.findAnnotation(annotationName: String): KSAnnotation? = annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
    }

    private fun KSFile.findFileAnnotation(annotationName: String): KSAnnotation? = annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
    }

    private fun KSAnnotated.hasAnnotation(annotationName: String): Boolean = findAnnotation(annotationName) != null

    private fun KSDeclaration.isAccessible(): Boolean = !modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.PROTECTED)

    private fun KSType.toTypeMeta(): OpenApiTypeMeta {
        val nonNull = makeNotNullable()
        val nonNullName = nonNull.declaration.qualifiedName?.asString() ?: nonNull.declaration.simpleName.asString()
        return OpenApiTypeMeta(
            qualifiedName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString(),
            displayName = renderType(),
            nonNullDisplayName = nonNull.renderType(),
            nullable = nullability == Nullability.NULLABLE,
            collectionLike = nonNullName in setOf(LIST, SET, COLLECTION, BYTE_ARRAY),
            mapLike = nonNullName == MAP,
            arguments = arguments.map { it.type?.resolve()?.toTypeMeta() ?: OpenApiTypeMeta.Star },
        )
    }

    private fun KSType.unwrapResponseEntity(): KSType {
        val declarationName = declaration.qualifiedName?.asString()
        if (declarationName != RESPONSE_ENTITY && declarationName != HTTP_ENTITY) {
            return this
        }
        return arguments.firstOrNull()?.type?.resolve() ?: this
    }

    private fun KSType.renderType(): String {
        val declarationName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
        if (arguments.isEmpty()) {
            return declarationName + if (nullability == Nullability.NULLABLE) "?" else ""
        }
        val renderedArgs = arguments.joinToString(", ") {
            when {
                it.variance == Variance.STAR -> "*"
                it.type == null -> "*"
                else -> it.type!!.resolve().renderType()
            }
        }
        return "$declarationName<$renderedArgs>" + if (nullability == Nullability.NULLABLE) "?" else ""
    }

    private fun KSType.isUnitType(): Boolean = declaration.qualifiedName?.asString() == UNIT

    private fun KSType.isMultipartFileType(): Boolean = declaration.qualifiedName?.asString() == MULTIPART_FILE

    private fun KSType.isMultipartFileListType(): Boolean {
        if (declaration.qualifiedName?.asString() != LIST || arguments.size != 1) return false
        return arguments.single().type?.resolve()?.declaration?.qualifiedName?.asString() == MULTIPART_FILE
    }

    private fun KSType.isSimpleBindableType(): Boolean {
        val nonNull = makeNotNullable()
        val declarationName = nonNull.declaration.qualifiedName?.asString() ?: return false
        if (declarationName in simpleBindableTypes) return true
        return (nonNull.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
    }

    private fun KSAnnotation.argument(name: String): KSValueArgument? = arguments.firstOrNull { it.name?.asString() == name }

    private fun KSFunctionDeclaration.signatureKey(): String {
        val ownerName = qualifiedName?.asString() ?: "${packageName.asString()}.${simpleName.asString()}"
        val parameterTypes = parameters.joinToString(",") { it.type.resolve().renderType() }
        return "$ownerName($parameterTypes)"
    }

    private fun warn(message: String, symbol: KSAnnotated? = null) {
        warnings += OpenApiExtractionWarning(
            framework = OpenApiFramework.SPRING,
            message = message,
            sourceFile = (symbol as? KSDeclaration)?.containingFile?.filePath ?: (symbol as? KSFile)?.filePath,
            ownerQualifiedName = (symbol as? KSDeclaration)?.qualifiedName?.asString(),
        )
        if (symbol == null) logger?.warn(message) else logger?.warn(message, symbol)
    }

    private data class HttpMapping(val method: OpenApiHttpMethod, val path: String)
    private data class MediaTypes(val consumes: List<String>, val produces: List<String>)
    private data class ParameterExtraction(val parameters: List<OpenApiParameterMeta>, val requestBody: OpenApiRequestBodyMeta?)
    private data class ParameterBinding(
        val kind: BindingKind,
        val location: OpenApiParameterLocation,
        val externalName: String,
        val required: Boolean,
    )

    private enum class BindingKind {
        CONTEXT,
        PATH,
        QUERY,
        BODY,
        HEADER,
        PART,
        UNKNOWN,
    }
}
