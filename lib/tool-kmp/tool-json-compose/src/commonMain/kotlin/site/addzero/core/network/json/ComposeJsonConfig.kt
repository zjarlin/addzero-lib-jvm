package site.addzero.core.network.json

import androidx.compose.material3.ColorScheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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

val composeScalarSerializersModule = SerializersModule {
    contextual(Dp::class, ComposeDpSerializer)
    contextual(TextUnit::class, ComposeTextUnitSerializer)
    contextual(PaddingValues::class, ComposePaddingValuesSerializer)
    contextual(BorderStroke::class, ComposeBorderStrokeSerializer)
    contextual(TextStyle::class, ComposeTextStyleSerializer)
    contextual(TextDecoration::class, ComposeTextDecorationSerializer)
    contextual(FontWeight::class, ComposeFontWeightSerializer)
    contextual(TextAlign::class, ComposeTextAlignSerializer)
    contextual(TextDirection::class, ComposeTextDirectionSerializer)
    contextual(TextIndent::class, ComposeTextIndentSerializer)
}

val composeSerializersModule = SerializersModule {
    include(composeColorSerializersModule)
    include(composeShapeSerializersModule)
    include(composeScalarSerializersModule)
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
