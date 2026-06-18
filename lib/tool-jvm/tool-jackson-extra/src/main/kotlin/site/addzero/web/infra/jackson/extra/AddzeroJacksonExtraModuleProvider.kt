package site.addzero.web.infra.jackson.extra

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.babyfish.jimmer.jackson.ImmutableModule
import site.addzero.web.infra.jackson.AddzeroJacksonModuleProvider
import site.addzero.web.infra.jackson.addzeroDateTimeFormatter
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime as KotlinxLocalDateTime

class AddzeroJacksonExtraModuleProvider : AddzeroJacksonModuleProvider {
    override fun modules(): List<Module> =
        listOf(
            SimpleModule().apply {
                addSerializer(KotlinxLocalDateTime::class.java, KotlinxLocalDateTimeSerializer())
                addDeserializer(KotlinxLocalDateTime::class.java, KotlinxLocalDateTimeDeserializer())
            },
            ImmutableModule(),
        )
}

private class KotlinxLocalDateTimeSerializer : JsonSerializer<KotlinxLocalDateTime>() {
    override fun serialize(value: KotlinxLocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(addzeroDateTimeFormatter.format(value.toJavaLocalDateTime()))
    }
}

private class KotlinxLocalDateTimeDeserializer : JsonDeserializer<KotlinxLocalDateTime>() {
    @OptIn(ExperimentalTime::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): KotlinxLocalDateTime {
        val text = p.text
        return if (text.isNullOrEmpty()) {
            kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        } else {
            try {
                LocalDateTime.parse(text, addzeroDateTimeFormatter).toKotlinLocalDateTime()
            } catch (e: Exception) {
                kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun getNullValue(ctxt: DeserializationContext?): KotlinxLocalDateTime =
        kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
