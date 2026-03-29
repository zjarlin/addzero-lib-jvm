package site.addzero.compose.zh.fonts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import site.addzero.compose.zh.fonts.generated.resources.Res
import site.addzero.compose.zh.fonts.generated.resources.noto_sans_cjk_sc_regular

@Composable
actual fun rememberChineseUiFontFamilyOrNull(): FontFamily? {
    val regularFont = Font(Res.font.noto_sans_cjk_sc_regular)
    return remember(regularFont) { FontFamily(regularFont) }
}
