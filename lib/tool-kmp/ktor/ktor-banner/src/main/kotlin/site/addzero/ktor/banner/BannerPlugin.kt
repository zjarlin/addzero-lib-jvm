package site.addzero.ktor.banner

import io.ktor.server.application.*

private const val RESET = "\u001B[0m"
private const val BOLD = "\u001B[1m"

/**
 * Ktor Banner 插件 — 启动时打印渐变色 ASCII Art
 *
 * ```kotlin
 * install(Banner) {
 *     text = "MY APP"
 *     showKtorVersion = true
 *     subtitle = "v1.0.0"
 *     gradient = KOTLIN_PURPLE_GRADIENT
 * }
 * ```
 */
val Banner = createApplicationPlugin(name = "Banner", createConfiguration = ::BannerConfig) {
    val config = pluginConfig
    val lines = FigletFont.render(config.text)
    val ktorVersion = if (config.showKtorVersion) detectKtorVersion() else null
    val gradient = config.gradient
    val total = lines.size.coerceAtLeast(1)

    val output = buildString {
        append("\n")
        lines.forEachIndexed { i, line ->
            val (r, g, b) = lerpGradient(gradient, i.toFloat() / (total - 1).coerceAtLeast(1))
            append("$BOLD\u001B[38;2;${r};${g};${b}m  $line$RESET\n")
        }
        val infoParts = mutableListOf<String>()
        if (ktorVersion != null) infoParts += "Ktor $ktorVersion"
        if (config.subtitle.isNotBlank()) infoParts += config.subtitle
        if (infoParts.isNotEmpty()) {
            val (r, g, b) = gradient[gradient.size / 2]
            append("\u001B[38;2;${r};${g};${b}m$BOLD  ⚡ ${infoParts.joinToString("  |  ")}$RESET\n")
        }
        append("\n")
    }

    println(output)
}

/**
 * Banner 插件配置
 */
class BannerConfig {
    /** 要渲染的文本（支持 A-Z 0-9 及部分符号） */
    var text: String = "KTOR"

    /** 是否显示 Ktor 版本号 */
    var showKtorVersion: Boolean = true

    /** 副标题（版本号、描述等） */
    var subtitle: String = ""

    /** 渐变色阶 (RGB)，默认 Kotlin 紫 */
    var gradient: List<Triple<Int, Int, Int>> = KOTLIN_PURPLE_GRADIENT
}

// ── 预置渐变 ─────────────────────────────────────────────────

/** Kotlin 风格紫色渐变 #C084FC → #7F52FF → #4F1D91 */
val KOTLIN_PURPLE_GRADIENT = listOf(
    Triple(192, 132, 252),
    Triple(167, 107, 252),
    Triple(143, 82, 255),
    Triple(127, 82, 255),  // Kotlin 紫
    Triple(111, 62, 220),
    Triple(95, 45, 185),
    Triple(79, 29, 145),
)

/** 日落渐变 */
val SUNSET_GRADIENT = listOf(
    Triple(255, 154, 158),
    Triple(254, 137, 118),
    Triple(253, 120, 80),
    Triple(250, 100, 60),
    Triple(220, 80, 50),
)

/** 海洋渐变 */
val OCEAN_GRADIENT = listOf(
    Triple(102, 217, 255),
    Triple(72, 187, 235),
    Triple(42, 157, 215),
    Triple(22, 127, 195),
    Triple(12, 97, 165),
)

/** 极光渐变 */
val AURORA_GRADIENT = listOf(
    Triple(0, 255, 136),
    Triple(0, 210, 180),
    Triple(0, 170, 220),
    Triple(80, 130, 255),
    Triple(140, 90, 255),
    Triple(180, 60, 220),
)

// ── 内部工具 ─────────────────────────────────────────────────

internal fun lerpGradient(
    stops: List<Triple<Int, Int, Int>>,
    t: Float,
): Triple<Int, Int, Int> {
    if (stops.size == 1) return stops[0]
    val clamped = t.coerceIn(0f, 1f)
    val segment = clamped * (stops.size - 1)
    val idx = segment.toInt().coerceAtMost(stops.size - 2)
    val frac = segment - idx
    val (r1, g1, b1) = stops[idx]
    val (r2, g2, b2) = stops[idx + 1]
    return Triple(
        (r1 + (r2 - r1) * frac).toInt(),
        (g1 + (g2 - g1) * frac).toInt(),
        (b1 + (b2 - b1) * frac).toInt(),
    )
}

internal fun detectKtorVersion(): String = try {
    Application::class.java.`package`?.implementationVersion
        ?: run {
            val props = java.util.Properties()
            val stream = Application::class.java.classLoader
                .getResourceAsStream("META-INF/maven/io.ktor/ktor-server-core/pom.properties")
            if (stream != null) { props.load(stream); props.getProperty("version", "unknown") }
            else "unknown"
        }
} catch (_: Exception) { "unknown" }
