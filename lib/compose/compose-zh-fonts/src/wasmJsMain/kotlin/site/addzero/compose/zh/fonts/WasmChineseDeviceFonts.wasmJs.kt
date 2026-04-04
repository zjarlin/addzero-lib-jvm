@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package site.addzero.compose.zh.fonts

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.files.Blob
import kotlin.js.Promise
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator

internal suspend fun loadPreferredChineseDeviceFontFamilyOrNull(
    fontFamilyResolver: FontFamily.Resolver,
): FontFamily? {
    if (!supportsQueryLocalFonts()) {
        return null
    }
    val rawFontList =
        runCatching { queryLocalFonts().await<JsAny>() }
            .getOrNull()
            ?: return null
    val availableFonts = collectDeviceFonts(rawFontList)
    if (availableFonts.isEmpty()) {
        return null
    }
    val fontFamily =
        preferredChineseDeviceFontCandidates.firstNotNullOfOrNull { candidate ->
            candidate.toFontFamilyOrNull(availableFonts)
        } ?: return null
    fontFamilyResolver.preload(fontFamily)
    return fontFamily
}

private fun collectDeviceFonts(rawFontList: JsAny): List<DeviceFontData> {
    if (!isJsArray(rawFontList)) {
        return emptyList()
    }
    val fontCount = jsArrayLength(rawFontList)
    return buildList(fontCount) {
        repeat(fontCount) { index ->
            val rawFont = jsArrayElement(rawFontList, index) ?: return@repeat
            add(DeviceFontData(rawFont))
        }
    }
}

private val preferredChineseDeviceFontCandidates =
    listOf(
        DeviceFontCandidate(
            familyNames = setOf("PingFang SC"),
            styleWeights =
                mapOf(
                    "ultralight" to FontWeight.ExtraLight,
                    "thin" to FontWeight.Thin,
                    "light" to FontWeight.Light,
                    "regular" to FontWeight.Normal,
                    "medium" to FontWeight.Medium,
                    "semibold" to FontWeight.SemiBold,
                ),
        ),
        DeviceFontCandidate(
            familyNames = setOf("STHeiti"),
            styleWeights =
                mapOf(
                    "light" to FontWeight.Light,
                    "regular" to FontWeight.Normal,
                ),
        ),
        DeviceFontCandidate(
            familyNames = setOf("Microsoft YaHei UI", "MicrosoftYaHeiUI"),
            styleWeights =
                mapOf(
                    "light" to FontWeight.Light,
                    "regular" to FontWeight.Normal,
                    "bold" to FontWeight.Bold,
                ),
        ),
        DeviceFontCandidate(
            familyNames = setOf("Microsoft YaHei", "MicrosoftYaHei"),
            styleWeights =
                mapOf(
                    "light" to FontWeight.Light,
                    "regular" to FontWeight.Normal,
                    "bold" to FontWeight.Bold,
                ),
        ),
    )

private data class DeviceFontCandidate(
    val familyNames: Set<String>,
    val styleWeights: Map<String, FontWeight>,
) {
    suspend fun toFontFamilyOrNull(
        availableFonts: List<DeviceFontData>,
    ): FontFamily? {
        val matchedFonts =
            availableFonts.filter { font ->
                familyNames.any { candidateFamily -> candidateFamily.equals(font.family, ignoreCase = true) }
            }
        if (matchedFonts.isEmpty()) {
            return null
        }
        val platformFonts =
            matchedFonts.mapNotNull { font ->
                val fontBytes = runCatching { font.readBytes() }.getOrNull() ?: return@mapNotNull null
                Font(
                    identity = font.identity,
                    data = fontBytes,
                    weight = styleWeights[font.normalizedStyle] ?: font.inferWeight(),
                )
            }
        return platformFonts.takeIf(List<*>::isNotEmpty)?.let(::FontFamily)
    }
}

private data class DeviceFontData(
    val rawFont: JsAny,
    val family: String = deviceFontFamily(rawFont).trim(),
    val style: String = deviceFontStyle(rawFont).trim(),
    val postscriptName: String = deviceFontPostscriptName(rawFont).trim(),
) {
    private var cachedBytes: ByteArray? = null

    val normalizedStyle = style.lowercase()

    val identity =
        postscriptName.ifBlank {
            buildString {
                append(family.ifBlank { "device-font" })
                if (style.isNotBlank()) {
                    append('-')
                    append(style)
                }
            }
        }

    suspend fun readBytes(): ByteArray {
        cachedBytes?.let { return it }
        val bytes = fontBlob(rawFont).await<Blob>().toByteArray()
        cachedBytes = bytes
        return bytes
    }

    private suspend fun Blob.toByteArray(): ByteArray {
        val arrayBuffer = blobArrayBuffer(this).await<ArrayBuffer>()
        return arrayBuffer.toByteArray()
    }

    fun inferWeight(): FontWeight =
        when {
            normalizedStyle.contains("semibold") -> FontWeight.SemiBold
            normalizedStyle.contains("bold") -> FontWeight.Bold
            normalizedStyle.contains("medium") -> FontWeight.Medium
            normalizedStyle.contains("ultralight") -> FontWeight.ExtraLight
            normalizedStyle.contains("thin") -> FontWeight.Thin
            normalizedStyle.contains("light") -> FontWeight.Light
            else -> FontWeight.Normal
        }
}

private fun ArrayBuffer.toByteArray(): ByteArray {
    val source = Int8Array(this, 0, byteLength)
    return jsInt8ArrayToKotlinByteArray(source)
}

private fun jsInt8ArrayToKotlinByteArray(
    source: Int8Array,
): ByteArray {
    val size = source.length
    @OptIn(UnsafeWasmMemoryApi::class)
    return withScopedMemoryAllocator { allocator ->
        val buffer = allocator.allocate(size)
        val destinationAddress = buffer.address.toInt()
        jsExportInt8ArrayToWasm(source, size, destinationAddress)
        ByteArray(size) { index -> (buffer + index).loadByte() }
    }
}

@JsFun("() => typeof globalThis.queryLocalFonts === 'function'")
private external fun supportsQueryLocalFonts(): Boolean

@JsFun("() => globalThis.queryLocalFonts()")
private external fun queryLocalFonts(): Promise<JsAny>

@JsFun("(value) => Array.isArray(value)")
private external fun isJsArray(value: JsAny): Boolean

@JsFun("(array) => array.length")
private external fun jsArrayLength(array: JsAny): Int

@JsFun("(array, index) => array[index]")
private external fun jsArrayElement(
    array: JsAny,
    index: Int,
): JsAny?

@JsFun("(font) => font.family ?? ''")
private external fun deviceFontFamily(font: JsAny): String

@JsFun("(font) => font.style ?? ''")
private external fun deviceFontStyle(font: JsAny): String

@JsFun("(font) => font.postscriptName ?? ''")
private external fun deviceFontPostscriptName(font: JsAny): String

@JsFun("(font) => font.blob()")
private external fun fontBlob(font: JsAny): Promise<Blob>

@JsFun("(blob) => blob.arrayBuffer()")
private external fun blobArrayBuffer(blob: Blob): Promise<ArrayBuffer>

@JsFun(
    """(src, size, dstAddr) => {
        const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
        mem8.set(src);
    }""",
)
private external fun jsExportInt8ArrayToWasm(
    src: Int8Array,
    size: Int,
    dstAddr: Int,
)
