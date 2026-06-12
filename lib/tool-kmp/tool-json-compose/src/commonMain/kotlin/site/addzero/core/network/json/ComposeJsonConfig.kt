package site.addzero.core.network.json

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val composeColorSerializersModule = SerializersModule {
    contextual(Color::class, ComposeColorArgbIntSerializer)
    contextual(ColorScheme::class, MaterialColorSchemeArgbIntSerializer)
}

val composeShapeSerializersModule = SerializersModule {
    contextual(Shape::class, ComposeShapeSerializer)
}

val composeSerializersModule = SerializersModule {
    include(composeColorSerializersModule)
    include(composeShapeSerializersModule)
}

fun Json.withComposeSerializers(): Json = Json(this) {
    serializersModule = SerializersModule {
        include(this@withComposeSerializers.serializersModule)
        include(composeSerializersModule)
    }
}

fun Json.withComposeColorSerializers(): Json = withComposeSerializers()

val composeJson: Json = json.withComposeSerializers()

val prettyComposeJson: Json = prettyJson.withComposeSerializers()

val strictComposeJson: Json = strictJson.withComposeSerializers()

val omitNullComposeJson: Json = omitNullJson.withComposeSerializers()
