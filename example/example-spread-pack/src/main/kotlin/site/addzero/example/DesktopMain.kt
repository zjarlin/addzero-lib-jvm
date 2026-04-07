package site.addzero.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
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
class MaterialTextProps

@Composable
@GenerateSpreadPackOverloads
fun WrappedMaterialText(
    @SpreadPack
    props: MaterialTextProps,
) {
    Text(
        text = "[Wrapped] ${props.text}",
        modifier = props.modifier,
        color = props.color,
        textAlign = props.textAlign,
    )
}

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Spread Pack Compose Text",
        ) {
            val autoExitMillis = System.getProperty("spread.pack.desktop.autoExitMillis")
                ?.toLongOrNull()
                ?.takeIf { it > 0L }
            if (autoExitMillis != null) {
                LaunchedEffect(autoExitMillis) {
                    delay(autoExitMillis)
                    exitApplication()
                }
            }
            MaterialTheme {
                SpreadPackDesktopApp()
            }
        }
    }
}

@Composable
private fun SpreadPackDesktopApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF7FAFF),
                        Color(0xFFE7F0FF),
                    ),
                ),
            )
            .padding(24.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.94f),
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "spread-pack + Material3 Text",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "下面三段文案都不是直接调用 Text，而是走 @SpreadPackCarrierOf(\"androidx.compose.material3.Text\") + @GenerateSpreadPackOverloads 生成的包装调用。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WrappedMaterialText(
                    text = "显式展开 text / modifier / color / textAlign",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
                WrappedMaterialText(
                    text = "这是二次封装后的标题文案",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1565C0),
                    textAlign = TextAlign.Center,
                )
                WrappedMaterialText(
                    text = "真实桌面窗口已启动，说明编译链路和运行链路都通过了。",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2E7D32),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
