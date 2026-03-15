package site.addzero.ksp.multireceiver

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class MultireceiverProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    object : SymbolProcessor {
      private val collected = linkedMapOf<String, WrapperModel>()
      private val logger = environment.logger
      private val codeGenerator = environment.codeGenerator

      override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferred = mutableListOf<KSAnnotated>()
        resolver.getSymbolsWithAnnotation(GENERATE_EXTENSION_FQCN).forEach { symbol ->
          val function = symbol as? KSFunctionDeclaration ?: return@forEach
          if (!function.validate()) {
            deferred += function
            return@forEach
          }
          function.toWrapperModel(logger)?.let { wrapper ->
            collected[wrapper.id] = wrapper
          }
        }
        return deferred
      }

      override fun finish() {
        collected.values
          .groupBy { wrapper -> wrapper.generatedFile }
          .forEach { (generatedFile, wrappers) ->
            generateFile(
              codeGenerator = codeGenerator,
              generatedFile = generatedFile,
              wrappers = wrappers,
            )
          }
      }
    }
}

private data class GeneratedFile(
  val packageName: String,
  val fileName: String,
)

private data class TypeParameterModel(
  val name: String,
  val bounds: List<String>,
)

private data class ParameterModel(
  val name: String,
  val type: String,
)

private data class WrapperModel(
  val id: String,
  val generatedFile: GeneratedFile,
  val originatingFile: KSFile,
  val functionName: String,
  val typeParameters: List<TypeParameterModel>,
  val contextParameters: List<ParameterModel>,
  val extensionReceiver: ParameterModel?,
  val valueParameters: List<ParameterModel>,
  val returnType: String,
  val isSuspend: Boolean,
  val callExpression: String,
)

private fun KSFunctionDeclaration.toWrapperModel(
  logger: KSPLogger,
): WrapperModel? {
  val containingFile = containingFile
  if (containingFile == null) {
    logger.error("Skipping @GenerateExtension function without containing file.", this)
    return null
  }
  if (parentDeclaration is KSFunctionDeclaration) {
    logger.error("@GenerateExtension does not support local functions.", this)
    return null
  }
  if (extensionReceiver != null) {
    logger.error("@GenerateExtension does not support existing extension receivers.", this)
    return null
  }
  if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
    logger.error("@GenerateExtension only supports public/internal declarations.", this)
    return null
  }
  if (parameters.any { parameter -> parameter.isVararg || parameter.hasDefault }) {
    logger.error("@GenerateExtension does not support vararg/default parameters yet.", this)
    return null
  }
  if (parameters.any { parameter -> parameter.type.resolve().isUnsupportedCallableType() }) {
    logger.error("@GenerateExtension does not support function-typed parameters yet.", this)
    return null
  }
  if (returnType?.resolve()?.isUnsupportedCallableType() == true) {
    logger.error("@GenerateExtension does not support function-typed return values yet.", this)
    return null
  }

  val ownerClass = parentDeclaration as? KSClassDeclaration
  if (ownerClass != null && ownerClass.classKind == ClassKind.ANNOTATION_CLASS) {
    logger.error("@GenerateExtension does not support annotation class members.", this)
    return null
  }

  val packageName = packageName.asString()
  val receiverParameters = parameters.filter { parameter -> parameter.hasReceiverAnnotation() }
  val remainingParameters = parameters.filterNot { parameter -> parameter.hasReceiverAnnotation() }
  val ownerTypeParameters = ownerClass.orEmptyTypeParameters()
  val functionTypeParameters = typeParameters.map { typeParameter ->
    typeParameter.toModel(packageName)
  }
  val allTypeParameters = ownerTypeParameters + functionTypeParameters
  val isSuspend = modifiers.contains(Modifier.SUSPEND)
  val returnTypeName = returnType?.resolve()?.renderType(packageName) ?: "kotlin.Unit"
  val generatedFile = GeneratedFile(
    packageName = packageName,
    fileName = containingFile.fileName.removeSuffix(".kt") + "Multireceiver",
  )
  val ownerContextParameter = ownerClass.toOwnerContextParameter(parameters)
  val callTarget = ownerClass.callTarget(ownerContextParameter?.name)

  val model = when {
    receiverParameters.isEmpty() && remainingParameters.size == 1 -> {
      val sourceParameter = remainingParameters.single()
      val callArguments = parameters.joinToString(", ") { parameter ->
        if (parameter == sourceParameter) {
          "this"
        } else {
          parameter.safeName()
        }
      }
      WrapperModel(
        id = buildWrapperId(this, ownerContextParameter != null, "extension"),
        generatedFile = generatedFile,
        originatingFile = containingFile,
        functionName = simpleName.asString(),
        typeParameters = allTypeParameters,
        contextParameters = listOfNotNull(ownerContextParameter),
        extensionReceiver = ParameterModel(
          name = sourceParameter.safeName(),
          type = sourceParameter.type.resolve().renderType(packageName),
        ),
        valueParameters = emptyList(),
        returnType = returnTypeName,
        isSuspend = isSuspend,
        callExpression = "$callTarget${simpleName.asString()}($callArguments)",
      )
    }
    receiverParameters.isNotEmpty() -> {
      val valueParameters = remainingParameters.map { parameter ->
        ParameterModel(
          name = parameter.safeName(),
          type = parameter.type.resolve().renderType(packageName),
        )
      }
      val contextParameters = buildList {
        ownerContextParameter?.let(::add)
        receiverParameters.forEach { parameter ->
          add(
            ParameterModel(
              name = parameter.safeName(),
              type = parameter.type.resolve().renderType(packageName),
            ),
          )
        }
      }
      val callArguments = parameters.joinToString(", ") { parameter -> parameter.safeName() }
      WrapperModel(
        id = buildWrapperId(this, ownerContextParameter != null, "context"),
        generatedFile = generatedFile,
        originatingFile = containingFile,
        functionName = simpleName.asString(),
        typeParameters = allTypeParameters,
        contextParameters = contextParameters,
        extensionReceiver = null,
        valueParameters = valueParameters,
        returnType = returnTypeName,
        isSuspend = isSuspend,
        callExpression = "$callTarget${simpleName.asString()}($callArguments)",
      )
    }
    else -> {
      logger.error(
        "@GenerateExtension requires exactly one non-@Receiver parameter or at least one @Receiver parameter.",
        this,
      )
      null
    }
  }

  return model
}

private fun buildWrapperId(
  function: KSFunctionDeclaration,
  hasOwnerContext: Boolean,
  kind: String,
): String {
  val signature = function.parameters.joinToString(",") { parameter ->
    parameter.type.resolve().renderType(function.packageName.asString())
  }
  val ownerPrefix = if (hasOwnerContext) "member" else "top"
  return buildString {
    append(function.qualifiedName?.asString() ?: function.simpleName.asString())
    append("#")
    append(ownerPrefix)
    append("#")
    append(kind)
    append("#")
    append(signature)
  }
}

private fun KSClassDeclaration?.orEmptyTypeParameters(): List<TypeParameterModel> {
  if (this == null) {
    return emptyList()
  }
  val packageName = packageName.asString()
  return typeParameters.map { typeParameter -> typeParameter.toModel(packageName) }
}

private fun KSClassDeclaration?.callTarget(ownerParameterName: String?): String {
  if (this == null) {
    return ""
  }
  return if (classKind == ClassKind.OBJECT || isCompanionObject) {
    qualifiedName?.asString().orEmpty() + "."
  } else {
    "${ownerParameterName.orEmpty()}."
  }
}

private fun KSClassDeclaration?.toOwnerContextParameter(
  parameters: List<KSValueParameter>,
): ParameterModel? {
  if (this == null) {
    return null
  }
  if (classKind == ClassKind.OBJECT || isCompanionObject) {
    return null
  }
  return ParameterModel(
    name = toOwnerParameterName(parameters),
    type = renderSelfType(),
  )
}

private fun KSClassDeclaration.toOwnerParameterName(
  parameters: List<KSValueParameter>,
): String {
  val usedNames = buildSet {
    parameters.mapTo(this) { parameter -> parameter.safeName() }
    typeParameters.mapTo(this) { typeParameter -> typeParameter.name.asString() }
  }
  val baseName = simpleName.asString()
    .replaceFirstChar { char -> char.lowercase() }
    .ifBlank { "owner" }
    .escapeIfKeyword()
  if (baseName !in usedNames) {
    return baseName
  }
  var suffix = 1
  while ("${baseName}Receiver$suffix" in usedNames) {
    suffix += 1
  }
  return "${baseName}Receiver$suffix"
}

private fun KSTypeParameter.toModel(packageName: String): TypeParameterModel {
  val filteredBounds = bounds
    .map { bound -> bound.resolve().renderType(packageName) }
    .filterNot { bound -> bound == "kotlin.Any?" || bound == "kotlin.Any" }
    .toList()
  return TypeParameterModel(
    name = name.asString(),
    bounds = filteredBounds,
  )
}

private fun KSClassDeclaration.renderSelfType(): String {
  val qualifiedName = qualifiedName?.asString() ?: simpleName.asString()
  if (typeParameters.isEmpty()) {
    return qualifiedName
  }
  val typeArguments = typeParameters.joinToString(prefix = "<", postfix = ">") { typeParameter ->
    typeParameter.name.asString()
  }
  return qualifiedName + typeArguments
}

private fun KSValueParameter.hasReceiverAnnotation(): Boolean {
  return annotations.any { annotation ->
    annotation.annotationType.resolve().declaration.qualifiedName?.asString() == RECEIVER_FQCN
  }
}

private fun KSValueParameter.safeName(): String {
  return name?.asString().orEmpty().ifBlank { "param" }.escapeIfKeyword()
}

private fun KSType.renderType(currentPackage: String): String {
  val declaration = declaration
  val declarationName = when (declaration) {
    is KSTypeParameter -> declaration.name.asString()
    else -> declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
  }
  val typeArguments = arguments.renderTypeArguments(currentPackage)
  val nullableSuffix = if (declaration is KSTypeParameter) {
    ""
  } else if (nullability == Nullability.NULLABLE) {
    "?"
  } else {
    ""
  }
  return declarationName + typeArguments + nullableSuffix
}

private fun List<KSTypeArgument>.renderTypeArguments(currentPackage: String): String {
  if (isEmpty()) {
    return ""
  }
  return joinToString(prefix = "<", postfix = ">") { argument ->
    if (argument.variance == Variance.STAR) {
      "*"
    } else {
      buildString {
        when (argument.variance) {
          Variance.CONTRAVARIANT -> append("in ")
          Variance.COVARIANT -> append("out ")
          Variance.INVARIANT,
          Variance.STAR -> Unit
        }
        append(argument.type?.resolve()?.renderType(currentPackage) ?: "kotlin.Any")
      }
    }
  }
}

private fun KSType.isUnsupportedCallableType(): Boolean {
  val qualifiedName = declaration.qualifiedName?.asString().orEmpty()
  return qualifiedName.startsWith("kotlin.Function") ||
    qualifiedName.startsWith("kotlin.reflect.KFunction") ||
    qualifiedName.startsWith("kotlin.coroutines.SuspendFunction")
}

private fun generateFile(
  codeGenerator: CodeGenerator,
  generatedFile: GeneratedFile,
  wrappers: List<WrapperModel>,
) {
  val file = codeGenerator.createNewFile(
    dependencies = Dependencies(
      aggregating = false,
      *wrappers.map { wrapper -> wrapper.originatingFile }.distinct().toTypedArray(),
    ),
    packageName = generatedFile.packageName,
    fileName = generatedFile.fileName,
  )

  file.bufferedWriter().use { writer ->
    writer.appendLine("@file:Suppress(\"unused\")")
    writer.appendLine()
    writer.appendLine("package ${generatedFile.packageName}")
    writer.appendLine()
    wrappers.forEachIndexed { index, wrapper ->
      if (index > 0) {
        writer.appendLine()
      }
      writer.append(wrapper.render())
      writer.appendLine()
    }
  }
}

private fun WrapperModel.render(): String {
  return buildString {
    if (contextParameters.isNotEmpty()) {
      append("context(")
      append(
        contextParameters.joinToString(", ") { parameter ->
          "${parameter.name}: ${parameter.type}"
        },
      )
      appendLine(")")
    }
    if (isSuspend) {
      append("suspend ")
    }
    append("fun")
    if (typeParameters.isNotEmpty()) {
      append(" <")
      append(
        typeParameters.joinToString(", ") { typeParameter ->
          if (typeParameter.bounds.isEmpty()) {
            typeParameter.name
          } else {
            "${typeParameter.name} : ${typeParameter.bounds.joinToString(" & ")}"
          }
        },
      )
      append(">")
    }
    append(" ")
    extensionReceiver?.let { receiver ->
      append("${receiver.type}.")
    }
    append(functionName)
    append("(")
    append(
      valueParameters.joinToString(", ") { parameter ->
        "${parameter.name}: ${parameter.type}"
      },
    )
    append("): ")
    append(returnType)
    append(" = ")
    append(callExpression)
  }
}

private fun String.escapeIfKeyword(): String {
  return if (this in KOTLIN_KEYWORDS) {
    "`$this`"
  } else {
    this
  }
}

private val KOTLIN_KEYWORDS = setOf(
  "as",
  "break",
  "class",
  "continue",
  "do",
  "else",
  "false",
  "for",
  "fun",
  "if",
  "in",
  "interface",
  "is",
  "null",
  "object",
  "package",
  "return",
  "super",
  "this",
  "throw",
  "true",
  "try",
  "typealias",
  "val",
  "var",
  "when",
  "while",
)

private const val GENERATE_EXTENSION_FQCN = "site.addzero.kcp.annotations.GenerateExtension"
private const val RECEIVER_FQCN = "site.addzero.kcp.annotations.Receiver"
