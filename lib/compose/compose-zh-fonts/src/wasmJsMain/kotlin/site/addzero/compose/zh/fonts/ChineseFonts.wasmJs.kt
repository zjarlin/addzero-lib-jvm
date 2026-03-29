package site.addzero.compose.zh.fonts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import site.addzero.compose.zh.fonts.generated.resources.Res
import site.addzero.compose.zh.fonts.generated.resources.noto_sans_cjk_sc_regular

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberChineseUiFontFamilyOrNull(): FontFamily? {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    var deviceFontState by remember(fontFamilyResolver) {
        mutableStateOf<WasmChineseFontState>(WasmChineseFontState.Loading)
    }
    val regularFont by preloadFont(Res.font.noto_sans_cjk_sc_regular)
    val fallbackFontFamily = regularFont?.let { loadedFont ->
        remember(loadedFont) { FontFamily(loadedFont) }
    }
    LaunchedEffect(fontFamilyResolver) {
        deviceFontState = WasmChineseFontState.Ready(loadPreferredChineseDeviceFontFamilyOrNull(fontFamilyResolver))
    }
    return when (val resolvedState = deviceFontState) {
        WasmChineseFontState.Loading -> null
        is WasmChineseFontState.Ready -> resolvedState.fontFamily ?: fallbackFontFamily
    }
}

private sealed interface WasmChineseFontState {
    data object Loading : WasmChineseFontState

    data class Ready(
        val fontFamily: FontFamily?,
    ) : WasmChineseFontState
}
