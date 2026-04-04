package site.addzero.compose.applecorner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppleRoundedDefaults {
    val XSmall = 12.dp
    val Small = 14.dp
    val Medium = 18.dp
    val Large = 22.dp
    val Pill = 999.dp
}

@Stable
fun appleRoundedShape(
    radius: Dp = AppleRoundedDefaults.Large,
): Shape = RoundedCornerShape(radius)

@Stable
fun Modifier.appleRoundedClip(
    radius: Dp = AppleRoundedDefaults.Large,
): Modifier = clip(appleRoundedShape(radius))

@Stable
fun Modifier.appleRoundedClip(
    shape: Shape,
): Modifier = clip(shape)

@Stable
fun Modifier.appleRounded(
    radius: Dp = AppleRoundedDefaults.Large,
    containerColor: Color = Color.Transparent,
    border: BorderStroke? = null,
): Modifier = appleRounded(
    shape = appleRoundedShape(radius),
    containerColor = containerColor,
    border = border,
)

@Stable
fun Modifier.appleRounded(
    shape: Shape,
    containerColor: Color = Color.Transparent,
    border: BorderStroke? = null,
): Modifier {
    var next = this
    if (border != null) {
        next = next.border(border = border, shape = shape)
    }
    next = next.background(color = containerColor, shape = shape)
    return next.clip(shape)
}
