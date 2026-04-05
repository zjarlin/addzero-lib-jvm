package site.addzero.component.text

import androidx.compose.foundation.text.TextAutoSize
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
import kotlin.test.Test
import kotlin.test.assertTrue
import site.addzero.kcp.spreadpack.GenerateSpreadPackOverloads
import site.addzero.kcp.spreadpack.SpreadPack
import site.addzero.kcp.spreadpack.SpreadPackCarrierOf

@SpreadPackCarrierOf(
    value = "androidx.compose.material3.Text",
    parameterTypes = [
        String::class,
        Modifier::class,
        Color::class,
        TextAutoSize::class,
        TextUnit::class,
        FontStyle::class,
        FontWeight::class,
        FontFamily::class,
        TextUnit::class,
        TextDecoration::class,
        TextAlign::class,
        TextUnit::class,
        TextOverflow::class,
        Boolean::class,
        Int::class,
        Int::class,
        Function1::class,
        TextStyle::class,
    ],
    exclude = [
        "autoSize",
        "fontSize",
        "fontStyle",
        "fontWeight",
        "fontFamily",
        "letterSpacing",
        "textDecoration",
        "lineHeight",
        "overflow",
        "softWrap",
        "maxLines",
        "minLines",
        "onTextLayout",
        "style",
    ],
)
class DirectTextProps

@Composable
@GenerateSpreadPackOverloads
fun DirectTextAlias(
    @SpreadPack
    props: DirectTextProps,
) {
    Text(
        text = props.text,
        modifier = props.modifier,
        color = props.color,
        textAlign = props.textAlign,
    )
}

@Composable
private fun compileSpreadPackCallSites() {
    H1(text = "标题")
    Caption(text = "说明", textAlign = TextAlign.Center)
    BlueText(text = "强调标题")
    DirectTextAlias(
        text = "直接借原生 Text 字段并展开",
        modifier = Modifier,
        color = Color.Unspecified,
        textAlign = null,
    )
}

class SpreadPackTextUsageTest {

    @Test
    fun spread_pack_text_wrappers_compile() {
        assertTrue(true)
    }
}
