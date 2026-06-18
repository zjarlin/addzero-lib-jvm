package site.addzero.web.infra.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.ServiceLoader

val addzeroDateTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
    .optionalEnd()
    .toFormatter()

@Deprecated(
    message = "Use addzeroDateTimeFormatter.",
    replaceWith = ReplaceWith("addzeroDateTimeFormatter"),
)
val dateTimeFormatter: DateTimeFormatter = addzeroDateTimeFormatter

fun addzeroJacksonModules(): List<Module> =
    listOfNotNull(
        KotlinModule.Builder()
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build(),
        JavaTimeModule().apply {
            addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(addzeroDateTimeFormatter))
            addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(addzeroDateTimeFormatter))
        },
    ) + ServiceLoader.load(AddzeroJacksonModuleProvider::class.java)
        .flatMap { it.modules() }

fun createAddzeroObjectMapper(base: ObjectMapper = ObjectMapper()): ObjectMapper =
    base
        .registerModules(addzeroJacksonModules())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)

@Configuration
class JacksonConfig {
    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder
                .modules(addzeroJacksonModules())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                    DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,
                )
        }
    }
}
