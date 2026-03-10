package site.addzero.springktor.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.validate

private const val REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController"
private const val REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping"
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
private const val COMPONENT = "org.springframework.stereotype.Component"
private const val SERVICE = "org.springframework.stereotype.Service"
private const val CONFIGURATION = "org.springframework.context.annotation.Configuration"
private const val BEAN = "org.springframework.context.annotation.Bean"

private const val APPLICATION_CALL = "io.ktor.server.application.ApplicationCall"
private const val APPLICATION = "io.ktor.server.application.Application"
private const val ROUTING_CONTEXT = "io.ktor.server.routing.RoutingContext"
private const val APPLICATION_REQUEST = "io.ktor.server.request.ApplicationRequest"
private const val APPLICATION_RESPONSE = "io.ktor.server.response.ApplicationResponse"
private const val MULTIPART_FILE = "org.springframework.web.multipart.MultipartFile"
private const val LIST = "kotlin.collections.List"

private val mappingAnnotations = linkedMapOf(
    GET_MAPPING to SpringHttpMethod.GET,
    POST_MAPPING to SpringHttpMethod.POST,
    PUT_MAPPING to SpringHttpMethod.PUT,
    DELETE_MAPPING to SpringHttpMethod.DELETE,
    PATCH_MAPPING to SpringHttpMethod.PATCH,
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

data class SpringKtorCollectResult(
    val model: SpringKtorModel,
    val deferred: List<KSAnnotated>,
    val hasErrors: Boolean,
)

class SpringKtorCollector(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) {
    private val topLevelRoutes = linkedMapOf<String, TopLevelRouteMeta>()
    private val controllerRoutes = linkedMapOf<String, ControllerRouteMeta>()
    private val beanClasses = linkedMapOf<String, BeanClassMeta>()
    private val beanFactories = linkedMapOf<String, BeanFactoryMeta>()
    private val deferredSymbols = linkedSetOf<KSAnnotated>()
    private var hasErrors = false

    fun collect(): SpringKtorCollectResult {
        collectMappedFunctions()
        collectBeanClasses()
        collectBeanFactories()
        validateCollisions()

        return SpringKtorCollectResult(
            model = SpringKtorModel(
                topLevelRoutes = topLevelRoutes.values.toSet(),
                controllerRoutes = controllerRoutes.values.toSet(),
                beanClasses = beanClasses.values.toSet(),
                beanFactories = beanFactories.values.toSet(),
            ),
            deferred = deferredSymbols.toList(),
            hasErrors = hasErrors,
        )
    }

    private fun collectMappedFunctions() {
        val visitedFunctions = linkedSetOf<String>()

        for ((annotationName, _) in mappingAnnotations) {
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
                .filterIsInstance<KSFunctionDeclaration>()

            for (function in symbols) {
                if (!function.validate()) {
                    deferredSymbols += function
                    continue
                }

                val functionKey = function.signatureKey()
                if (!visitedFunctions.add(functionKey)) {
                    continue
                }

                val routeMeta = extractRouteMeta(function) ?: continue
                when (routeMeta) {
                    is TopLevelRouteMeta -> {
                        topLevelRoutes[functionKey] = routeMeta
                    }

                    is ControllerRouteMeta -> {
                        controllerRoutes[functionKey] = routeMeta
                    }
                }
            }
        }
    }

    private fun collectBeanClasses() {
        val visitedClasses = linkedSetOf<String>()
        val beanAnnotations = listOf(COMPONENT, SERVICE, CONFIGURATION, REST_CONTROLLER)

        for (annotationName in beanAnnotations) {
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
                .filterIsInstance<KSClassDeclaration>()

            for (clazz in symbols) {
                if (!clazz.validate()) {
                    deferredSymbols += clazz
                    continue
                }

                val qualifiedName = clazz.qualifiedName?.asString() ?: continue
                if (!visitedClasses.add(qualifiedName)) {
                    continue
                }

                val beanMeta = extractBeanClassMeta(clazz) ?: continue
                beanClasses[qualifiedName] = beanMeta
            }
        }
    }

    private fun collectBeanFactories() {
        val visitedFactories = linkedSetOf<String>()
        val symbols = resolver.getSymbolsWithAnnotation(BEAN)
            .filterIsInstance<KSFunctionDeclaration>()

        for (function in symbols) {
            if (!function.validate()) {
                deferredSymbols += function
                continue
            }

            val parent = function.parentDeclaration as? KSClassDeclaration
            if (parent == null) {
                error("`@Bean` only supports instance methods inside `@Configuration` classes.", function)
                continue
            }
            if (!parent.hasAnnotation(CONFIGURATION)) {
                error("`@Bean` method `${function.simpleName.asString()}` must live inside a `@Configuration` class.", function)
                continue
            }
            if (!function.isAccessible()) {
                error("`@Bean` method `${function.simpleName.asString()}` must not be private or protected.", function)
                continue
            }
            if (function.typeParameters.isNotEmpty()) {
                error("Generic `@Bean` method `${function.simpleName.asString()}` is not supported.", function)
                continue
            }
            if (function.extensionReceiver != null) {
                error("Extension `@Bean` method `${function.simpleName.asString()}` is not supported.", function)
                continue
            }
            if (function.returnType == null || function.returnType!!.resolve().isUnitType()) {
                error("`@Bean` method `${function.simpleName.asString()}` must return a bean type.", function)
                continue
            }

            val configurationQualifiedName = parent.qualifiedName?.asString() ?: continue
            val factoryKey = "$configurationQualifiedName#${function.signatureKey()}"
            if (!visitedFactories.add(factoryKey)) {
                continue
            }

            beanFactories[factoryKey] = BeanFactoryMeta(
                configurationQualifiedName = configurationQualifiedName,
                configurationSimpleName = parent.simpleName.asString(),
                methodName = function.simpleName.asString(),
                dependencyCount = function.parameters.size,
                sourceFilePath = function.sourceFilePath(),
            )
        }
    }

    private fun extractRouteMeta(function: KSFunctionDeclaration): Any? {
        if (function.extensionReceiver != null) {
            error("Spring Ktor does not support extension route functions: `${function.simpleName.asString()}`.", function)
            return null
        }
        if (!function.isAccessible()) {
            error("Route handler `${function.simpleName.asString()}` must not be private or protected.", function)
            return null
        }

        val mapping = extractHttpMapping(function) ?: return null
        val parameters = extractParameters(function) ?: return null
        val requestBodyCount = parameters.count { it.bindingKind == ParameterBindingKind.REQUEST_BODY }
        val multipartCount = parameters.count {
            it.bindingKind == ParameterBindingKind.MULTIPART_FILE ||
                it.bindingKind == ParameterBindingKind.MULTIPART_FILE_LIST ||
                it.bindingKind == ParameterBindingKind.REQUEST_PART_VALUE
        }

        if (requestBodyCount > 1) {
            error("Route handler `${function.simpleName.asString()}` can only have one `@RequestBody` parameter.", function)
            return null
        }
        if (requestBodyCount > 0 && multipartCount > 0) {
            error("Route handler `${function.simpleName.asString()}` cannot mix `@RequestBody` and multipart parts.", function)
            return null
        }

        val returnType = function.returnType?.resolve()
        val returnsUnit = returnType == null || returnType.isUnitType()
        val nullableReturn = returnType?.nullability == Nullability.NULLABLE
        val parent = function.parentDeclaration

        if (parent == null) {
            val qualifiedName = function.qualifiedName?.asString()
            if (qualifiedName == null) {
                error("Only top-level named functions can be generated as Spring Ktor routes.", function)
                return null
            }
            val fileName = function.containingFile?.fileName?.removeSuffix(".kt")
            if (fileName.isNullOrBlank()) {
                error("Unable to resolve source file for `${function.simpleName.asString()}`.", function)
                return null
            }

            return TopLevelRouteMeta(
                packageName = function.packageName.asString(),
                fileName = fileName,
                functionName = function.simpleName.asString(),
                functionQualifiedName = qualifiedName,
                httpMethod = mapping.first,
                path = mapping.second,
                parameters = parameters,
                returnsUnit = returnsUnit,
                nullableReturn = nullableReturn,
                isSuspend = function.modifiers.contains(Modifier.SUSPEND),
                sourceFilePath = function.sourceFilePath(),
            )
        }

        val controller = parent as? KSClassDeclaration
        if (controller == null) {
            error("Unsupported parent declaration for `${function.simpleName.asString()}`.", function)
            return null
        }
        if (!controller.hasAnnotation(REST_CONTROLLER)) {
            error(
                "Mapped method `${function.simpleName.asString()}` must be top-level or live inside a `@RestController` class.",
                function,
            )
            return null
        }
        if (controller.typeParameters.isNotEmpty()) {
            error("Generic controller `${controller.simpleName.asString()}` is not supported.", controller)
            return null
        }

        val controllerQualifiedName = controller.qualifiedName?.asString() ?: return null
        val basePath = extractBasePath(controller) ?: return null
        val fullPath = combinePaths(basePath, mapping.second)

        return ControllerRouteMeta(
            controllerPackageName = controller.packageName.asString(),
            controllerSimpleName = controller.simpleName.asString(),
            controllerQualifiedName = controllerQualifiedName,
            functionName = function.simpleName.asString(),
            httpMethod = mapping.first,
            path = fullPath,
            parameters = parameters,
            returnsUnit = returnsUnit,
            nullableReturn = nullableReturn,
            isSuspend = function.modifiers.contains(Modifier.SUSPEND),
            sourceFilePath = function.sourceFilePath(),
        )
    }

    private fun extractBeanClassMeta(clazz: KSClassDeclaration): BeanClassMeta? {
        if (!clazz.isAccessible()) {
            error("Bean class `${clazz.simpleName.asString()}` must not be private or protected.", clazz)
            return null
        }
        if (clazz.typeParameters.isNotEmpty()) {
            error("Generic bean class `${clazz.simpleName.asString()}` is not supported.", clazz)
            return null
        }
        if (clazz.classKind !in setOf(ClassKind.CLASS, ClassKind.OBJECT)) {
            error("Bean declaration `${clazz.simpleName.asString()}` must be a class or object.", clazz)
            return null
        }

        val kind = when {
            clazz.hasAnnotation(CONFIGURATION) -> BeanClassKind.CONFIGURATION
            clazz.hasAnnotation(REST_CONTROLLER) -> BeanClassKind.CONTROLLER
            else -> BeanClassKind.COMPONENT
        }
        val constructorParamCount = clazz.injectionConstructorParameterCount() ?: return null
        val qualifiedName = clazz.qualifiedName?.asString() ?: return null

        return BeanClassMeta(
            kind = kind,
            qualifiedName = qualifiedName,
            simpleName = clazz.simpleName.asString(),
            packageName = clazz.packageName.asString(),
            objectDeclaration = clazz.classKind == ClassKind.OBJECT,
            dependencyCount = constructorParamCount,
            sourceFilePath = clazz.sourceFilePath(),
        )
    }

    private fun extractParameters(function: KSFunctionDeclaration): List<ParameterMeta>? {
        return buildList {
            for ((index, parameter) in function.parameters.withIndex()) {
                val parameterName = parameter.name?.asString()
                if (parameterName.isNullOrBlank()) {
                    error("Unnamed parameter in `${function.simpleName.asString()}` is not supported.", function)
                    return null
                }
                val resolvedType = parameter.type.resolve()
                val bindingMeta = determineBinding(parameter, resolvedType) ?: return null
                add(
                    ParameterMeta(
                        index = index,
                        sourceName = parameterName,
                        externalName = bindingMeta.externalName,
                        typeName = bindingMeta.typeName,
                        nonNullTypeName = bindingMeta.nonNullTypeName,
                        bindingKind = bindingMeta.bindingKind,
                        nullable = resolvedType.nullability == Nullability.NULLABLE,
                    )
                )
            }
        }
    }

    private fun determineBinding(parameter: KSValueParameter, resolvedType: com.google.devtools.ksp.symbol.KSType): BindingExtraction? {
        val parameterName = parameter.name?.asString() ?: return null
        val typeName = resolvedType.renderType()
        val nonNullTypeName = resolvedType.makeNotNullable().renderType()
        val typeQualifiedName = resolvedType.declaration.qualifiedName?.asString()

        val contextBinding = when (typeQualifiedName) {
            APPLICATION_CALL -> ParameterBindingKind.APPLICATION_CALL
            APPLICATION -> ParameterBindingKind.APPLICATION
            ROUTING_CONTEXT -> ParameterBindingKind.ROUTING_CONTEXT
            APPLICATION_REQUEST -> ParameterBindingKind.APPLICATION_REQUEST
            APPLICATION_RESPONSE -> ParameterBindingKind.APPLICATION_RESPONSE
            else -> null
        }
        if (contextBinding != null) {
            return BindingExtraction(contextBinding, parameterName, typeName, nonNullTypeName)
        }

        val pathVariable = parameter.findAnnotation(PATH_VARIABLE)
        if (pathVariable != null) {
            if (!resolvedType.isSimpleBindableType()) {
                error("`@PathVariable` parameter `$parameterName` must be a simple scalar or enum type.", parameter)
                return null
            }
            return BindingExtraction(
                bindingKind = ParameterBindingKind.PATH_VARIABLE,
                externalName = extractNamedValue(pathVariable, parameterName),
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }

        val requestParam = parameter.findAnnotation(REQUEST_PARAM)
        if (requestParam != null) {
            if (!resolvedType.isSimpleBindableType()) {
                error("`@RequestParam` parameter `$parameterName` must be a simple scalar or enum type.", parameter)
                return null
            }
            return BindingExtraction(
                bindingKind = ParameterBindingKind.REQUEST_PARAM,
                externalName = extractNamedValue(requestParam, parameterName),
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }

        val requestBody = parameter.findAnnotation(REQUEST_BODY)
        if (requestBody != null) {
            return BindingExtraction(
                bindingKind = ParameterBindingKind.REQUEST_BODY,
                externalName = parameterName,
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }

        val requestHeader = parameter.findAnnotation(REQUEST_HEADER)
        if (requestHeader != null) {
            if (!resolvedType.isSimpleBindableType()) {
                error("`@RequestHeader` parameter `$parameterName` must be a simple scalar or enum type.", parameter)
                return null
            }
            return BindingExtraction(
                bindingKind = ParameterBindingKind.REQUEST_HEADER,
                externalName = extractNamedValue(requestHeader, parameterName),
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }

        val requestPart = parameter.findAnnotation(REQUEST_PART)
        if (requestPart != null) {
            val externalName = extractNamedValue(requestPart, parameterName)
            if (resolvedType.isMultipartFileType()) {
                return BindingExtraction(
                    bindingKind = ParameterBindingKind.MULTIPART_FILE,
                    externalName = externalName,
                    typeName = typeName,
                    nonNullTypeName = nonNullTypeName,
                )
            }
            if (resolvedType.isMultipartFileListType()) {
                return BindingExtraction(
                    bindingKind = ParameterBindingKind.MULTIPART_FILE_LIST,
                    externalName = externalName,
                    typeName = typeName,
                    nonNullTypeName = nonNullTypeName,
                )
            }
            if (!resolvedType.isSimpleBindableType()) {
                error(
                    "`@RequestPart` parameter `$parameterName` only supports simple scalar values, MultipartFile, or List<MultipartFile> in MVP.",
                    parameter,
                )
                return null
            }
            return BindingExtraction(
                bindingKind = ParameterBindingKind.REQUEST_PART_VALUE,
                externalName = externalName,
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }

        if (resolvedType.isMultipartFileType()) {
            return BindingExtraction(
                bindingKind = ParameterBindingKind.MULTIPART_FILE,
                externalName = parameterName,
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }
        if (resolvedType.isMultipartFileListType()) {
            return BindingExtraction(
                bindingKind = ParameterBindingKind.MULTIPART_FILE_LIST,
                externalName = parameterName,
                typeName = typeName,
                nonNullTypeName = nonNullTypeName,
            )
        }
        if (!resolvedType.isSimpleBindableType()) {
            error(
                "Unannotated parameter `$parameterName` of type `$typeName` is unsupported. Use a Spring binding annotation or a Ktor context type.",
                parameter,
            )
            return null
        }

        return BindingExtraction(
            bindingKind = ParameterBindingKind.REQUEST_PARAM,
            externalName = parameterName,
            typeName = typeName,
            nonNullTypeName = nonNullTypeName,
        )
    }

    private fun extractHttpMapping(function: KSFunctionDeclaration): Pair<SpringHttpMethod, String>? {
        for ((annotationName, fixedMethod) in mappingAnnotations) {
            val annotation = function.findAnnotation(annotationName) ?: continue
            val path = extractMappingPath(annotation) ?: return null
            if (fixedMethod != null) {
                return fixedMethod to path
            }

            val methods = extractRequestMethods(annotation)
            if (methods.size > 1) {
                error("`@RequestMapping` on `${function.simpleName.asString()}` supports only one HTTP method in MVP.", function)
                return null
            }
            val httpMethod = methods.firstOrNull() ?: SpringHttpMethod.GET
            return httpMethod to path
        }
        return null
    }

    private fun extractBasePath(controller: KSClassDeclaration): String? {
        val annotation = controller.findAnnotation(REQUEST_MAPPING) ?: return ""
        return extractMappingPath(annotation)
    }

    private fun extractMappingPath(annotation: KSAnnotation): String? {
        val pathValues = extractStringValues(annotation.argument("path")).filter { it.isNotBlank() }
        val valueValues = extractStringValues(annotation.argument("value")).filter { it.isNotBlank() }
        val selected = when {
            pathValues.isNotEmpty() -> pathValues
            valueValues.isNotEmpty() -> valueValues
            else -> emptyList()
        }

        if (selected.size > 1) {
            error("Only a single mapping path is supported in MVP.")
            return null
        }

        return normalizePath(selected.firstOrNull().orEmpty())
    }

    private fun extractRequestMethods(annotation: KSAnnotation): List<SpringHttpMethod> {
        val argument = annotation.argument("method") ?: return emptyList()
        val values = when (val rawValue = argument.value) {
            is List<*> -> rawValue.mapNotNull { it?.toString() }
            null -> emptyList()
            else -> listOf(rawValue.toString())
        }

        return values.mapNotNull { value ->
            runCatching { SpringHttpMethod.valueOf(value.substringAfterLast('.')) }.getOrNull()
        }
    }

    private fun validateCollisions() {
        val fileCollisions = topLevelRoutes.values.groupBy { it.fileName }
            .filterValues { routes -> routes.map { it.packageName }.distinct().size > 1 }

        for ((fileName, routes) in fileCollisions) {
            for (route in routes) {
                error(
                    "Top-level source file name collision for `$fileName`. Duplicate file names across packages are not supported in MVP.",
                )
            }
        }

        val controllerCollisions = controllerRoutes.values.groupBy { it.controllerSimpleName }
            .filterValues { routes -> routes.map { it.controllerQualifiedName }.distinct().size > 1 }

        for ((controllerName, routes) in controllerCollisions) {
            for (route in routes) {
                error(
                    "Controller simple name collision for `$controllerName`. Duplicate controller simple names are not supported in MVP.",
                )
            }
        }

        val routeCollisions = linkedMapOf<String, MutableList<String>>()
        topLevelRoutes.values.forEach {
            routeCollisions.getOrPut("${it.httpMethod}:${it.path}") { mutableListOf() }
                .add(it.functionQualifiedName)
        }
        controllerRoutes.values.forEach {
            routeCollisions.getOrPut("${it.httpMethod}:${it.path}") { mutableListOf() }
                .add("${it.controllerQualifiedName}#${it.functionName}")
        }

        for ((signature, handlers) in routeCollisions) {
            if (handlers.size > 1) {
                error(
                    "Duplicate Spring route mapping `$signature` detected: ${handlers.joinToString()}",
                    null,
                )
            }
        }
    }

    private fun KSClassDeclaration.injectionConstructorParameterCount(): Int? {
        if (classKind == ClassKind.OBJECT) {
            return 0
        }

        val primaryConstructor = primaryConstructor
        if (primaryConstructor != null) {
            if (!primaryConstructor.isAccessible()) {
                error("Primary constructor for `${simpleName.asString()}` must not be private or protected.", this)
                return null
            }
            return primaryConstructor.parameters.size
        }

        val visibleConstructors = declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.simpleName.asString() == "<init>" }
            .filter { it.isAccessible() }
            .toList()

        if (visibleConstructors.size > 1) {
            error("Bean class `${simpleName.asString()}` has multiple visible constructors; MVP requires a single constructor.", this)
            return null
        }
        if (visibleConstructors.size == 1) {
            return visibleConstructors.single().parameters.size
        }

        return 0
    }

    private fun extractNamedValue(annotation: KSAnnotation, fallbackName: String): String {
        val value = extractStringValues(annotation.argument("name"))
            .filter { it.isNotBlank() }
            .ifEmpty {
                extractStringValues(annotation.argument("value")).filter { it.isNotBlank() }
            }
            .ifEmpty {
                annotation.arguments
                    .flatMap { extractStringValues(it) }
                    .filter { it.isNotBlank() }
            }
            .firstOrNull()
            .orEmpty()

        return value.ifBlank { fallbackName }
    }

    private fun extractStringValues(argument: KSValueArgument?): List<String> {
        if (argument == null) {
            return emptyList()
        }

        return when (val rawValue = argument.value) {
            is String -> listOf(rawValue)
            is List<*> -> rawValue.filterIsInstance<String>()
            null -> emptyList()
            else -> emptyList()
        }
    }

    private fun combinePaths(basePath: String, methodPath: String): String {
        val normalizedBase = normalizePath(basePath)
        val normalizedMethod = normalizePath(methodPath)

        if (normalizedBase == "/" && normalizedMethod == "/") {
            return "/"
        }
        if (normalizedBase == "/") {
            return normalizedMethod
        }
        if (normalizedMethod == "/") {
            return normalizedBase
        }

        return (normalizedBase.trimEnd('/') + "/" + normalizedMethod.trimStart('/'))
            .replace("//", "/")
    }

    private fun normalizePath(path: String): String {
        val trimmed = path.trim()
        if (trimmed.isBlank()) {
            return "/"
        }

        val withLeadingSlash = if (trimmed.startsWith('/')) {
            trimmed
        } else {
            "/$trimmed"
        }
        return withLeadingSlash.replace("//", "/")
    }

    private fun KSValueParameter.findAnnotation(annotationName: String): KSAnnotation? {
        return annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
        }
    }

    private fun KSAnnotated.findAnnotation(annotationName: String): KSAnnotation? {
        return annotations.firstOrNull {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
        }
    }

    private fun KSAnnotated.hasAnnotation(annotationName: String): Boolean {
        return findAnnotation(annotationName) != null
    }

    private fun KSDeclaration.isAccessible(): Boolean {
        return !modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.PROTECTED)
    }

    private fun com.google.devtools.ksp.symbol.KSType.renderType(): String {
        val declarationName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
        if (arguments.isEmpty()) {
            return declarationName + if (nullability == Nullability.NULLABLE) "?" else ""
        }

        val renderedArguments = arguments.joinToString(", ") { argument ->
            when {
                argument.variance == Variance.STAR -> "*"
                argument.type == null -> "*"
                else -> argument.type!!.resolve().renderType()
            }
        }
        val nullableSuffix = if (nullability == Nullability.NULLABLE) "?" else ""
        return "$declarationName<$renderedArguments>$nullableSuffix"
    }

    private fun com.google.devtools.ksp.symbol.KSType.isUnitType(): Boolean {
        return declaration.qualifiedName?.asString() == "kotlin.Unit"
    }

    private fun com.google.devtools.ksp.symbol.KSType.isMultipartFileType(): Boolean {
        return declaration.qualifiedName?.asString() == MULTIPART_FILE
    }

    private fun com.google.devtools.ksp.symbol.KSType.isMultipartFileListType(): Boolean {
        val declarationName = declaration.qualifiedName?.asString()
        if (declarationName != LIST || arguments.size != 1) {
            return false
        }

        return arguments.single().type?.resolve()?.declaration?.qualifiedName?.asString() == MULTIPART_FILE
    }

    private fun com.google.devtools.ksp.symbol.KSType.isSimpleBindableType(): Boolean {
        val nonNullType = makeNotNullable()
        val declarationName = nonNullType.declaration.qualifiedName?.asString() ?: return false
        if (simpleBindableTypes.contains(declarationName)) {
            return true
        }

        return (nonNullType.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
    }

    private fun KSAnnotation.argument(name: String): KSValueArgument? {
        return arguments.firstOrNull { it.name?.asString() == name }
    }

    private fun KSFunctionDeclaration.signatureKey(): String {
        val ownerName = qualifiedName?.asString()
            ?: "${packageName.asString()}.${simpleName.asString()}"
        val parameterTypes = parameters.joinToString(",") { parameter ->
            parameter.type.resolve().renderType()
        }
        return "$ownerName($parameterTypes)"
    }

    private fun KSDeclaration.sourceFilePath(): String {
        return containingFile?.filePath ?: qualifiedName?.asString().orEmpty()
    }

    private fun error(message: String, symbol: KSAnnotated? = null) {
        hasErrors = true
        if (symbol == null) {
            logger.error(message)
            return
        }
        logger.error(message, symbol)
    }

    private data class BindingExtraction(
        val bindingKind: ParameterBindingKind,
        val externalName: String,
        val typeName: String,
        val nonNullTypeName: String,
    )
}
