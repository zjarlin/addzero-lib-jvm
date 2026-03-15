package site.addzero.springktor.runtime

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.toByteArray
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.math.BigInteger
import java.util.UUID

suspend inline fun <reified T : Any> ApplicationCall.requireRequestBody(): T {
    return receive<T>()
}

suspend inline fun <reified T : Any> ApplicationCall.optionalRequestBody(): T? {
    return receiveNullable<T>()
}

inline fun <reified T : Any> ApplicationCall.requirePathVariable(name: String): T {
    return SpringValueConverters.require(parameters[name], "path variable", name)
}

inline fun <reified T : Any> ApplicationCall.optionalPathVariable(name: String): T? {
    return SpringValueConverters.optional(parameters[name], "path variable", name)
}

inline fun <reified T : Any> ApplicationCall.requireRequestParam(name: String): T {
    return SpringValueConverters.require(request.queryParameters[name], "request parameter", name)
}

inline fun <reified T : Any> ApplicationCall.optionalRequestParam(name: String): T? {
    return SpringValueConverters.optional(request.queryParameters[name], "request parameter", name)
}

inline fun <reified T : Any> ApplicationCall.requireRequestHeader(name: String): T {
    return SpringValueConverters.require(request.headers[name], "request header", name)
}

inline fun <reified T : Any> ApplicationCall.optionalRequestHeader(name: String): T? {
    return SpringValueConverters.optional(request.headers[name], "request header", name)
}

suspend fun ApplicationCall.receiveMultipartParts(): SpringMultipartParts {
    val multipartData = receiveMultipart()
    return multipartData.toMultipartParts()
}

suspend fun MultiPartData.toMultipartParts(): SpringMultipartParts {
    val files = linkedMapOf<String, MutableList<MultipartFile>>()
    val values = linkedMapOf<String, MutableList<String>>()

    while (true) {
        val part = readPart() ?: break
        try {
            when (part) {
                is PartData.FileItem -> {
                    val partName = part.name?.takeIf { it.isNotBlank() }
                        ?: throw IllegalArgumentException("Multipart file part is missing a name.")
                    val multipartFile = part.toSpringMultipartFile(partName)
                    files.getOrPut(partName) { mutableListOf() }.add(multipartFile)
                }

                is PartData.FormItem -> {
                    val partName = part.name?.takeIf { it.isNotBlank() }
                        ?: throw IllegalArgumentException("Multipart form part is missing a name.")
                    values.getOrPut(partName) { mutableListOf() }.add(part.value)
                }

                else -> {
                }
            }
        } finally {
            part.dispose()
        }
    }

    return SpringMultipartParts(files, values)
}

suspend fun ApplicationCall.respondGeneratedRouteResult(
    result: Any?,
    resultType: TypeInfo? = null,
) {
    if (response.isCommitted) {
        return
    }

    when (result) {
        null -> {
            respond(HttpStatusCode.NoContent)
        }

        Unit -> {
            respond(HttpStatusCode.OK)
        }

        else -> {
            respond(HttpStatusCode.OK, result, resultType ?: result.toTypeInfo())
        }
    }
}

private fun Any.toTypeInfo(): TypeInfo {
    return TypeInfo(
        type = this::class,
    )
}

data class SpringMultipartParts(
    @PublishedApi internal val files: Map<String, List<MultipartFile>>,
    @PublishedApi internal val values: Map<String, List<String>>,
) {
    fun requireFile(name: String): MultipartFile {
        return files[name]?.firstOrNull()
            ?: throw IllegalArgumentException("Required multipart file '$name' is missing.")
    }

    fun optionalFile(name: String): MultipartFile? {
        return files[name]?.firstOrNull()
    }

    fun requireFiles(name: String): List<MultipartFile> {
        return files[name].orEmpty().ifEmpty {
            throw IllegalArgumentException("Required multipart files '$name' are missing.")
        }
    }

    fun optionalFiles(name: String): List<MultipartFile> {
        return files[name].orEmpty()
    }

    inline fun <reified T : Any> requireValue(name: String): T {
        val rawValue = values[name]?.firstOrNull()
            ?: throw IllegalArgumentException("Required multipart value '$name' is missing.")
        return SpringValueConverters.convert(rawValue, "multipart value", name)
    }

    inline fun <reified T : Any> optionalValue(name: String): T? {
        return values[name]?.firstOrNull()?.let {
            SpringValueConverters.convert(it, "multipart value", name)
        }
    }
}

@PublishedApi
internal object SpringValueConverters {
    @PublishedApi
    internal inline fun <reified T : Any> require(rawValue: String?, source: String, name: String): T {
        val nonNullValue = rawValue
            ?: throw IllegalArgumentException("Required $source '$name' is missing.")
        return convert(nonNullValue, source, name)
    }

    @PublishedApi
    internal inline fun <reified T : Any> optional(rawValue: String?, source: String, name: String): T? {
        return rawValue?.let { convert(it, source, name) }
    }

    @PublishedApi
    internal inline fun <reified T : Any> convert(rawValue: String, source: String, name: String): T {
        val converted = convertValue(T::class.java, rawValue, source, name)
        @Suppress("UNCHECKED_CAST")
        return converted as T
    }

    @PublishedApi
    internal fun convertValue(targetClass: Class<*>, rawValue: String, source: String, name: String): Any {
        return when (targetClass) {
            String::class.java -> rawValue
            Int::class.java, Integer::class.java -> rawValue.toIntOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Int.")

            Long::class.java, java.lang.Long::class.java -> rawValue.toLongOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Long.")

            Short::class.java, java.lang.Short::class.java -> rawValue.toShortOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Short.")

            Byte::class.java, java.lang.Byte::class.java -> rawValue.toByteOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Byte.")

            Double::class.java, java.lang.Double::class.java -> rawValue.toDoubleOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Double.")

            Float::class.java, java.lang.Float::class.java -> rawValue.toFloatOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Float.")

            Boolean::class.java, java.lang.Boolean::class.java -> parseBoolean(rawValue, source, name)
            Char::class.java, java.lang.Character::class.java -> rawValue.singleOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to Char.")

            BigDecimal::class.java -> rawValue.toBigDecimalOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to BigDecimal.")

            BigInteger::class.java -> rawValue.toBigIntegerOrNull()
                ?: throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to BigInteger.")

            UUID::class.java -> runCatching { UUID.fromString(rawValue) }.getOrElse {
                throw IllegalArgumentException("Cannot convert $source '$name' value '$rawValue' to UUID.", it)
            }

            else -> convertEnum(targetClass, rawValue, source, name)
                ?: throw UnsupportedOperationException(
                    "Unsupported $source binding type '${targetClass.name}' for parameter '$name'."
                )
        }
    }

    private fun parseBoolean(rawValue: String, source: String, name: String): Boolean {
        return when (rawValue.lowercase()) {
            "true", "1", "yes", "on" -> true
            "false", "0", "no", "off" -> false
            else -> throw IllegalArgumentException(
                "Cannot convert $source '$name' value '$rawValue' to Boolean."
            )
        }
    }

    private fun convertEnum(targetClass: Class<*>, rawValue: String, source: String, name: String): Any? {
        if (!targetClass.isEnum) {
            return null
        }

        return targetClass.enumConstants
            ?.firstOrNull { (it as Enum<*>).name == rawValue }
            ?: throw IllegalArgumentException(
                "Cannot convert $source '$name' value '$rawValue' to enum '${targetClass.name}'."
            )
    }
}

internal suspend fun PartData.FileItem.toSpringMultipartFile(partName: String): MultipartFile {
    val bytes = provider().toByteArray()
    return ByteArraySpringMultipartFile(
        name = partName,
        originalFilename = originalFileName,
        contentType = contentType?.toString(),
        bytes = bytes,
    )
}
