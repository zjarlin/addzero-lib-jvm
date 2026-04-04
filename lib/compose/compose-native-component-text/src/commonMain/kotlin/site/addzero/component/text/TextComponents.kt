package site.addzero.component.text

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf

@Suppress("UnusedPrivateMember")
private fun materialTextArgsSource(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
}

@SpreadPackCarrierOf(
    value = "site.addzero.component.text.materialTextArgsSource",
)
class MaterialTextProps

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
    val props = MaterialTextProps()
    props.text = text
    props.modifier = modifier
    props.color = color
    props.textAlign = textAlign
    return props
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
@GenerateSpreadPackOverloads
private fun H1Packed(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun H2Packed(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun H3Packed(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun H4Packed(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun BodyLargePacked(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun BodyMediumPacked(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun BodySmallPacked(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun CaptionPacked(
    @SpreadPack
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
@GenerateSpreadPackOverloads
private fun BlueTextPacked(
    @SpreadPack
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
