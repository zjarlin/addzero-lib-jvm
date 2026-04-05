package site.addzero.component.text

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MaterialTextProps(
    val text: String,
    val modifier: Modifier = Modifier,
    val color: Color = Color.Unspecified,
    val textAlign: TextAlign? = null,
)

private fun Color.orDefault(defaultColor: Color): Color {
    return if (this == Color.Unspecified) {
        defaultColor
    } else {
        this
    }
}

private fun materialTextProps(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?,
): MaterialTextProps {
    return MaterialTextProps(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
    )
}

@Composable
private fun renderTypographyText(
    props: MaterialTextProps,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    defaultColor: Color,
) {
    Text(
        text = props.text,
        modifier = props.modifier,
        color = props.color.orDefault(defaultColor),
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = props.textAlign,
    )
}

@Composable
fun H1(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    H1Packed(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun H1Packed(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun H2(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    H2Packed(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun H2Packed(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun H3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    H3Packed(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun H3Packed(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun H4(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    H4Packed(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun H4Packed(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun BodyLarge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    BodyLargePacked(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun BodyLargePacked(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun BodyMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    BodyMediumPacked(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun BodyMediumPacked(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun BodySmall(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    BodySmallPacked(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun BodySmallPacked(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun Caption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    CaptionPacked(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun CaptionPacked(
    props: MaterialTextProps,
) {
    renderTypographyText(
        props = props,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        defaultColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun BlueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    BlueTextPacked(
        props = materialTextProps(
            text = text,
            modifier = modifier,
            color = color,
            textAlign = textAlign,
        ),
    )
}

@Composable
private fun BlueTextPacked(
    props: MaterialTextProps,
) {
    Text(
        text = props.text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = props.color.orDefault(MaterialTheme.colorScheme.primary),
        modifier = props.modifier.then(Modifier.padding(vertical = 8.dp)),
        textAlign = props.textAlign ?: TextAlign.Center,
    )
}
