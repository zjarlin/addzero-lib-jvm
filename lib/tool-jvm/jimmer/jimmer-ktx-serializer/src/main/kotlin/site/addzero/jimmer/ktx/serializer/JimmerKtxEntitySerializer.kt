package site.addzero.jimmer.ktx.serializer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.serializerOrNull
import org.babyfish.jimmer.ImmutableObjects
import org.babyfish.jimmer.jackson.ConverterMetadata
import org.babyfish.jimmer.meta.ImmutableProp
import org.babyfish.jimmer.meta.ImmutableType
import org.babyfish.jimmer.runtime.DraftSpi
import org.babyfish.jimmer.runtime.ImmutableSpi
import org.babyfish.jimmer.runtime.Internal
import java.lang.reflect.Array as ReflectArray
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Time
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.ZonedDateTime
import java.util.Date
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.UUID
import kotlin.reflect.KClass

class JimmerKtxEntitySerializer<T : Any>(
  private val type: KClass<T>,
  private val config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
) : KSerializer<T> {

  private val immutableType: ImmutableType = ImmutableType.tryGet(type.java)
    ?: throw IllegalArgumentException("${type.qualifiedName} 不是 Jimmer 实体类型")

  override val descriptor: SerialDescriptor by lazy(LazyThreadSafetyMode.NONE) {
    buildClassSerialDescriptor(immutableType.javaClass.name) {
      immutableType.props.values.forEach { prop ->
        element<JsonElement>(prop.name, isOptional = true)
      }
    }
  }

  override fun serialize(encoder: Encoder, value: T) {
    val jsonEncoder = encoder as? JsonEncoder
      ?: throw SerializationException("JimmerKtxEntitySerializer 只能配合 Json 使用")
    val codec = JimmerKtxCodec(
      json = jsonEncoder.json,
      serializersModule = encoder.serializersModule,
      config = config,
    )
    jsonEncoder.encodeJsonElement(codec.encodeEntity(value as ImmutableSpi, immutableType))
  }

  override fun deserialize(decoder: Decoder): T {
    val jsonDecoder = decoder as? kotlinx.serialization.json.JsonDecoder
      ?: throw SerializationException("JimmerKtxEntitySerializer 只能配合 Json 使用")
    val jsonElement = jsonDecoder.decodeJsonElement()
    val codec = JimmerKtxCodec(
      json = jsonDecoder.json,
      serializersModule = decoder.serializersModule,
      config = config,
    )
    return codec.decodeEntity(immutableType, jsonElement) as T
  }
}

fun <T : Any> jimmerKtxSerializer(
  type: KClass<T>,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): KSerializer<T> = JimmerKtxEntitySerializer(type, config)

inline fun <reified T : Any> jimmerKtxSerializer(
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): KSerializer<T> = jimmerKtxSerializer(T::class, config)

fun <T : Any> SerializersModuleBuilder.contextualJimmerEntity(
  type: KClass<T>,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
) {
  contextual(type, jimmerKtxSerializer(type, config))
}

inline fun <reified T : Any> SerializersModuleBuilder.contextualJimmerEntity(
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
) {
  contextualJimmerEntity(T::class, config)
}

fun SerializersModuleBuilder.contextualJimmerEntities(
  vararg types: KClass<out Any>,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
) {
  types.forEach { type ->
    @Suppress("UNCHECKED_CAST")
    contextualJimmerEntity(type as KClass<Any>, config)
  }
}

fun jimmerKtxSerializersModule(
  vararg types: KClass<out Any>,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
) = kotlinx.serialization.modules.SerializersModule {
  contextualJimmerEntities(*types, config = config)
}

fun <T : Any> Json.encodeJimmerToString(
  type: KClass<T>,
  value: T,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): String = encodeToString(jimmerKtxSerializer(type, config), value)

inline fun <reified T : Any> Json.encodeJimmerToString(
  value: T,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): String {
  val jimmerType = (value as? ImmutableSpi)?.__type()?.javaClass?.kotlin
  @Suppress("UNCHECKED_CAST")
  val finalType = (jimmerType ?: T::class) as KClass<T>
  return encodeJimmerToString(finalType, value, config)
}

fun <T : Any> Json.decodeJimmerFromString(
  type: KClass<T>,
  content: String,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): T = decodeFromString(jimmerKtxSerializer(type, config), content)

inline fun <reified T : Any> Json.decodeJimmerFromString(
  content: String,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): T = decodeJimmerFromString(T::class, content, config)

fun Json.withJimmerEntities(
  vararg types: KClass<out Any>,
  config: JimmerKtxSerializerConfig = JimmerKtxSerializerConfig(),
): Json = Json(this) {
  serializersModule = kotlinx.serialization.modules.SerializersModule {
    include(this@withJimmerEntities.serializersModule)
    contextualJimmerEntities(*types, config = config)
  }
}

private class JimmerKtxCodec(
  private val json: Json,
  private val serializersModule: SerializersModule,
  private val config: JimmerKtxSerializerConfig,
) {

  fun encodeEntity(spi: ImmutableSpi, immutableType: ImmutableType = spi.__type()): JsonObject {
    val content = LinkedHashMap<String, JsonElement>()
    immutableType.props.values.forEach { prop ->
      if (!spi.__isLoaded(prop.id)) {
        return@forEach
      }
      if (config.skipInvisibleProperties && !spi.__isVisible(prop.id)) {
        return@forEach
      }
      val rawValue = spi.__get(prop.id)
      content[prop.name] = encodeProp(prop, rawValue)
    }
    return JsonObject(content)
  }

  fun decodeEntity(immutableType: ImmutableType, element: JsonElement): Any {
    if (element !is JsonObject) {
      return decodeIdOnlyEntity(immutableType, element)
    }
    return Internal.produce(immutableType, null) { draft ->
      val draftSpi = draft as DraftSpi
      element.forEach { (name, valueElement) ->
        val prop = immutableType.props[name]
        if (prop == null) {
          if (config.ignoreUnknownProperties) {
            return@forEach
          }
          throw SerializationException("Jimmer 实体 ${immutableType.javaClass.name} 不存在属性 $name")
        }
        if (!prop.isMutable) {
          if (config.ignoreReadonlyProperties) {
            return@forEach
          }
          throw SerializationException("Jimmer 属性 ${immutableType.javaClass.name}.$name 不是可写属性")
        }
        val propValue = decodeProp(prop, valueElement)
        draftSpi.__set(prop.id, propValue)
      }
    }
  }

  private fun encodeProp(prop: ImmutableProp, value: Any?): JsonElement {
    val converterMetadata = prop.toConverterMetadata()
    if (converterMetadata == null) {
      return encodeByType(prop.genericType, value)
    }
    if (value == null) {
      return JsonNull
    }
    val converted = converterMetadata.getConverter<Any, Any>().output(value)
    return encodeByType(converterMetadata.targetType, converted)
  }

  private fun decodeProp(prop: ImmutableProp, element: JsonElement): Any? {
    val converterMetadata = prop.toConverterMetadata()
    if (converterMetadata == null) {
      return decodeByType(prop.genericType, element)
    }
    val converted = decodeByType(converterMetadata.targetType, element)
    if (converted == null) {
      return null
    }
    return converterMetadata.getConverter<Any, Any>().input(converted)
  }

  private fun encodeByType(type: Type, value: Any?): JsonElement {
    if (value == null) {
      return JsonNull
    }
    when (type) {
      is WildcardType -> {
        val actualType = type.upperBounds.firstOrNull() ?: Any::class.java
        return encodeByType(actualType, value)
      }

      is GenericArrayType -> {
        val componentType = type.genericComponentType
        val elements = (value as kotlin.Array<*>).map { encodeByType(componentType, it) }
        return JsonArray(elements)
      }

      is ParameterizedType -> {
        val rawClass = type.rawType as? Class<*>
        if (rawClass == null) {
          return encodeWithSerializer(type, value)
        }
        if (Collection::class.java.isAssignableFrom(rawClass)) {
          val elementType = type.actualTypeArguments.firstOrNull() ?: Any::class.java
          return JsonArray((value as Collection<*>).map { encodeByType(elementType, it) })
        }
        if (Map::class.java.isAssignableFrom(rawClass)) {
          return encodeMap(type, value as Map<*, *>)
        }
        return encodeWithSerializer(type, value)
      }

      is Class<*> -> {
        val immutableType = ImmutableType.tryGet(type)
        if (immutableType != null && value is ImmutableSpi) {
          return encodeEntity(value, immutableType)
        }
        if (type.isArray && !type.componentType.isPrimitive) {
          val elements = (value as kotlin.Array<*>).map { encodeByType(type.componentType, it) }
          return JsonArray(elements)
        }
        encodeCommonScalar(type, value)?.let { return it }
        return encodeWithSerializer(type, value)
      }
    }
    return encodeWithSerializer(type, value)
  }

  private fun decodeByType(type: Type, element: JsonElement): Any? {
    if (element is JsonNull) {
      return null
    }
    return when (type) {
      is WildcardType -> {
        val actualType = type.upperBounds.firstOrNull() ?: Any::class.java
        decodeByType(actualType, element)
      }

      is GenericArrayType -> decodeArray(type.genericComponentType, element)

      is ParameterizedType -> {
        val rawClass = type.rawType as? Class<*>
        if (rawClass == null) {
          decodeWithSerializer(type, element)
        } else {
        when {
          Collection::class.java.isAssignableFrom(rawClass) -> decodeCollection(rawClass, type.actualTypeArguments.first(), element)
          Map::class.java.isAssignableFrom(rawClass) -> decodeMap(type, element)
          else -> decodeWithSerializer(type, element)
        }
        }
      }

      is Class<*> -> {
        val immutableType = ImmutableType.tryGet(type)
        if (immutableType != null) {
          decodeEntity(immutableType, element)
        } else if (type.isArray && !type.componentType.isPrimitive) {
          decodeArray(type.componentType, element)
        } else {
          decodeCommonScalar(type, element) ?: decodeWithSerializer(type, element)
        }
      }

      else -> decodeWithSerializer(type, element)
    }
  }

  private fun encodeWithSerializer(type: Type, value: Any): JsonElement {
    val serializer = serializersModule.serializerOrNull(type)
      ?: throw SerializationException("找不到类型 ${type.typeName} 的 kotlinx 序列化器")
    return json.encodeToJsonElement(serializer as SerializationStrategy<Any>, value)
  }

  private fun decodeWithSerializer(type: Type, element: JsonElement): Any {
    val serializer = serializersModule.serializerOrNull(type)
      ?: throw SerializationException("找不到类型 ${type.typeName} 的 kotlinx 反序列化器")
    return json.decodeFromJsonElement(serializer as DeserializationStrategy<Any>, element)
  }

  private fun decodeCollection(rawClass: Class<*>, elementType: Type, element: JsonElement): Any {
    val array = element as? JsonArray
      ?: throw SerializationException("类型 ${rawClass.name} 需要 JSON 数组")
    val values = array.map { decodeByType(elementType, it) }
    return when {
      Set::class.java.isAssignableFrom(rawClass) -> LinkedHashSet(values)
      else -> values
    }
  }

  private fun decodeArray(componentType: Type, element: JsonElement): Any {
    val array = element as? JsonArray
      ?: throw SerializationException("数组类型需要 JSON 数组")
    val componentClass = componentType.rawClassOrNull()
      ?: throw SerializationException("无法解析数组元素类型 ${componentType.typeName}")
    val result = ReflectArray.newInstance(componentClass, array.size)
    array.forEachIndexed { index, item ->
      ReflectArray.set(result, index, decodeByType(componentType, item))
    }
    return result
  }

  private fun encodeMap(type: ParameterizedType, value: Map<*, *>): JsonObject {
    val keyType = type.actualTypeArguments[0]
    val valueType = type.actualTypeArguments[1]
    val content = LinkedHashMap<String, JsonElement>()
    value.forEach { (key, item) ->
      content[encodeMapKey(keyType, key)] = encodeByType(valueType, item)
    }
    return JsonObject(content)
  }

  private fun decodeMap(type: ParameterizedType, element: JsonElement): Map<Any?, Any?> {
    val keyType = type.actualTypeArguments[0]
    val valueType = type.actualTypeArguments[1]
    val jsonObject = element as? JsonObject
      ?: throw SerializationException("Map 类型需要 JSON 对象")
    val content = LinkedHashMap<Any?, Any?>()
    jsonObject.forEach { (key, valueElement) ->
      content[decodeMapKey(keyType, key)] = decodeByType(valueType, valueElement)
    }
    return content
  }

  private fun encodeMapKey(type: Type, key: Any?): String {
    if (key == null) {
      throw SerializationException("JSON 对象的 key 不能为 null")
    }
    val rawClass = type.rawClassOrNull()
    return when {
      rawClass == null -> key.toString()
      rawClass == String::class.java -> key as String
      rawClass == Char::class.java || rawClass == Char::class.javaObjectType -> key.toString()
      rawClass.isEnum -> (key as Enum<*>).name
      rawClass == UUID::class.java -> key.toString()
      Number::class.java.isAssignableFrom(rawClass) || rawClass.isPrimitive || rawClass == Boolean::class.javaObjectType -> key.toString()
      else -> throw SerializationException("Map key 类型 ${rawClass.name} 不能映射为 JSON 对象 key")
    }
  }

  private fun decodeMapKey(type: Type, key: String): Any {
    val rawClass = type.rawClassOrNull() ?: return key
    return when {
      rawClass == String::class.java -> key
      rawClass == Char::class.java || rawClass == Char::class.javaObjectType -> key.singleOrNull()
        ?: throw SerializationException("无法将 $key 解析为 Char")
      rawClass == Byte::class.java || rawClass == Byte::class.javaObjectType || rawClass == Byte::class.javaPrimitiveType -> key.toByte()
      rawClass == Short::class.java || rawClass == Short::class.javaObjectType || rawClass == Short::class.javaPrimitiveType -> key.toShort()
      rawClass == Int::class.java || rawClass == Int::class.javaObjectType || rawClass == Int::class.javaPrimitiveType -> key.toInt()
      rawClass == Long::class.java || rawClass == Long::class.javaObjectType || rawClass == Long::class.javaPrimitiveType -> key.toLong()
      rawClass == Float::class.java || rawClass == Float::class.javaObjectType || rawClass == Float::class.javaPrimitiveType -> key.toFloat()
      rawClass == Double::class.java || rawClass == Double::class.javaObjectType || rawClass == Double::class.javaPrimitiveType -> key.toDouble()
      rawClass == Boolean::class.java || rawClass == Boolean::class.javaObjectType || rawClass == Boolean::class.javaPrimitiveType -> key.toBooleanStrict()
      rawClass == UUID::class.java -> UUID.fromString(key)
      rawClass.isEnum -> decodeEnum(rawClass, JsonPrimitive(key))
      else -> throw SerializationException("Map key 类型 ${rawClass.name} 不能从 JSON 对象 key 反序列化")
    }
  }

  private fun decodeIdOnlyEntity(immutableType: ImmutableType, element: JsonElement): Any {
    val idProp = immutableType.idProp
      ?: throw SerializationException("Jimmer 实体 ${immutableType.javaClass.name} 没有 id 属性，无法从标量创建 id-only 对象")
    val idValue = decodeByType(idProp.genericType, element)
    return ImmutableObjects.makeIdOnly(immutableType, idValue)
      ?: throw SerializationException("无法创建 ${immutableType.javaClass.name} 的 id-only 对象")
  }

  private fun encodeCommonScalar(type: Class<*>, value: Any): JsonElement? {
    return when (value) {
      is JsonElement -> value
      is String -> JsonPrimitive(value)
      is Char -> JsonPrimitive(value.toString())
      is Boolean -> JsonPrimitive(value)
      is Byte -> JsonPrimitive(value)
      is Short -> JsonPrimitive(value)
      is Int -> JsonPrimitive(value)
      is Long -> JsonPrimitive(value)
      is Float -> JsonPrimitive(value)
      is Double -> JsonPrimitive(value)
      is BigInteger -> JsonPrimitive(value)
      is BigDecimal -> JsonPrimitive(value)
      is Enum<*> -> JsonPrimitive(value.name)
      is UUID -> JsonPrimitive(value.toString())
      is Instant -> JsonPrimitive(value.toString())
      is LocalDate -> JsonPrimitive(value.toString())
      is LocalDateTime -> JsonPrimitive(value.toString())
      is LocalTime -> JsonPrimitive(value.toString())
      is OffsetDateTime -> JsonPrimitive(value.toString())
      is OffsetTime -> JsonPrimitive(value.toString())
      is ZonedDateTime -> JsonPrimitive(value.toString())
      is Duration -> JsonPrimitive(value.toString())
      is Period -> JsonPrimitive(value.toString())
      is Timestamp -> JsonPrimitive(value.toInstant().toString())
      is java.sql.Date -> JsonPrimitive(value.toLocalDate().toString())
      is Time -> JsonPrimitive(value.toLocalTime().toString())
      is Date -> JsonPrimitive(value.toInstant().toString())
      else -> if (type.isEnum) JsonPrimitive((value as Enum<*>).name) else null
    }
  }

  private fun decodeCommonScalar(type: Class<*>, element: JsonElement): Any? {
    val primitive = element as? JsonPrimitive ?: return null
    return when (type) {
      String::class.java -> primitive.content
      Char::class.java, Char::class.javaObjectType -> primitive.content.singleOrNull()
        ?: throw SerializationException("无法将 ${primitive.content} 解析为 Char")
      Boolean::class.java, Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType -> primitive.booleanOrNull
        ?: primitive.content.toBooleanStrict()
      Byte::class.java, Byte::class.javaObjectType, Byte::class.javaPrimitiveType -> primitive.content.toByte()
      Short::class.java, Short::class.javaObjectType, Short::class.javaPrimitiveType -> primitive.content.toShort()
      Int::class.java, Int::class.javaObjectType, Int::class.javaPrimitiveType -> primitive.intOrNull ?: primitive.content.toInt()
      Long::class.java, Long::class.javaObjectType, Long::class.javaPrimitiveType -> primitive.content.toLong()
      Float::class.java, Float::class.javaObjectType, Float::class.javaPrimitiveType -> primitive.content.toFloat()
      Double::class.java, Double::class.javaObjectType, Double::class.javaPrimitiveType -> primitive.content.toDouble()
      BigInteger::class.java -> primitive.content.toBigInteger()
      BigDecimal::class.java -> primitive.content.toBigDecimal()
      UUID::class.java -> UUID.fromString(primitive.content)
      Instant::class.java -> Instant.parse(primitive.content)
      LocalDate::class.java -> LocalDate.parse(primitive.content)
      LocalDateTime::class.java -> LocalDateTime.parse(primitive.content)
      LocalTime::class.java -> LocalTime.parse(primitive.content)
      OffsetDateTime::class.java -> OffsetDateTime.parse(primitive.content)
      OffsetTime::class.java -> OffsetTime.parse(primitive.content)
      ZonedDateTime::class.java -> ZonedDateTime.parse(primitive.content)
      Duration::class.java -> Duration.parse(primitive.content)
      Period::class.java -> Period.parse(primitive.content)
      Timestamp::class.java -> Timestamp.from(Instant.parse(primitive.content))
      java.sql.Date::class.java -> java.sql.Date.valueOf(LocalDate.parse(primitive.content))
      Time::class.java -> Time.valueOf(LocalTime.parse(primitive.content))
      Date::class.java -> Date.from(Instant.parse(primitive.content))
      else -> if (type.isEnum) decodeEnum(type, primitive) else null
    }
  }

  private fun decodeEnum(type: Class<*>, primitive: JsonPrimitive): Any {
    val content = primitive.content
    return type.enumConstants.firstOrNull { (it as Enum<*>).name == content }
      ?: throw SerializationException("枚举 ${type.name} 不存在值 $content")
  }

  private fun ImmutableProp.toConverterMetadata(): ConverterMetadata? {
    val metadata = converterMetadata ?: return null
    return if (isScalarList) metadata.toListMetadata() else metadata
  }

  private fun Type.rawClassOrNull(): Class<*>? = when (this) {
    is Class<*> -> this
    is ParameterizedType -> rawType as? Class<*>
    is GenericArrayType -> ReflectArray.newInstance(genericComponentType.rawClassOrNull() ?: return null, 0).javaClass
    is WildcardType -> upperBounds.firstOrNull()?.rawClassOrNull()
    else -> null
  }
}
