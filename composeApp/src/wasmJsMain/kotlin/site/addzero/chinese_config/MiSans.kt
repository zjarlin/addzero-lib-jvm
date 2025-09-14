package site.addzero.chinese_config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response

var fonts = listOf(
    "DouyinSansBold.ttf",
//    "LXGWWenKai-Medium.ttf",
//    "PingFang-Medium.ttf"
).map { "./$it" }


fun ArrayBuffer.toTheByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

suspend fun loadRes(url: String): JsAny {
    val await = window.fetch(url).await<Response>().arrayBuffer().await<ArrayBuffer>()
    return await
}


lateinit var miFontFamily: FontFamily

@Composable
fun ChineseContainer(block:@Composable ()->Unit) {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val fontLodaded = remember { mutableStateOf(false) }
    if (fontLodaded.value) {
        block()
    }
//    var font = org.jetbrains.compose.resources.Font(Res.font.MiSans_Normal)

    LaunchedEffect(Unit) {
        val mapIndexed = fonts.mapIndexed { i, it ->
            val miSansBytes = loadRes(it).unsafeCast<ArrayBuffer>()
                .toTheByteArray()
            val element = Font("Font$i", miSansBytes)
            element
        }

        val fontFamily = FontFamily(mapIndexed)
        fontFamilyResolver.preload(fontFamily)
        miFontFamily = fontFamily
        fontLodaded.value = true
    }
}
