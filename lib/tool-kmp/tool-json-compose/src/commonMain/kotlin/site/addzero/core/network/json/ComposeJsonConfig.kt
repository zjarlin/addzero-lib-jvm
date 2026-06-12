package site.addzero.core.network.json

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val composeColorSerializersModule = SerializersModule {
    contextual(Color::class, ComposeColorArgbIntSerializer)
    contextual(ColorScheme::class, MaterialColorSchemeArgbIntSerializer)
}

fun Json.withComposeColorSerializers(): Json = Json(this) {
    serializersModule = SerializersModule {
        include(this@withComposeColorSerializers.serializersModule)
        include(composeColorSerializersModule)
    }
}

val composeJson: Json = json.withComposeColorSerializers()

val prettyComposeJson: Json = prettyJson.withComposeColorSerializers()

val strictComposeJson: Json = strictJson.withComposeColorSerializers()

val omitNullComposeJson: Json = omitNullJson.withComposeColorSerializers()
