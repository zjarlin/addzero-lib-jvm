package site.addzero.workbench.immersivedesktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.disableTitleBar
import site.addzero.appsidebar.LocalWorkbenchWindowFrame
import site.addzero.appsidebar.WorkbenchWindowFrame
import java.awt.Container
import javax.swing.JComponent

const val DefaultImmersiveDesktopToggleSystemPropertyKey = "site.addzero.window.macos.immersive.enabled"

/**
 * macOS 沉浸式工作台窗口配置。
 */
@Immutable
data class MacOsImmersiveDesktopWindowConfig(
  val topBarHeight: Dp,
  val leadingInset: Dp = 0.dp,
  val trailingInset: Dp = 0.dp,
  val toggleSystemPropertyKey: String? = DefaultImmersiveDesktopToggleSystemPropertyKey,
)

/**
 * 桌面端窗口宿主运行时兼容配置。
 */
fun configureImmersiveDesktopRuntime() {
  System.setProperty("sun.java2d.metal", System.getProperty("sun.java2d.metal") ?: "false")
}

/**
 * 在 macOS 下为工作台窗口注入沉浸式标题栏能力。
 */
@Composable
fun FrameWindowScope.ProvideMacOsImmersiveDesktopWindowFrame(
  state: WindowState,
  config: MacOsImmersiveDesktopWindowConfig,
  content: @Composable () -> Unit,
) {
  val immersiveEnabled = shouldEnableImmersiveTopBar(
    state = state,
    toggleSystemPropertyKey = config.toggleSystemPropertyKey,
  )
  val immersiveFrame = remember(config) {
    WorkbenchWindowFrame(
      immersiveTopBar = true,
      topBarHeight = config.topBarHeight,
      leadingInset = config.leadingInset,
      trailingInset = config.trailingInset,
    )
  }

  DisposableEffect(window, immersiveEnabled, config.topBarHeight) {
    if (immersiveEnabled) {
      window.findSkiaLayer()?.disableTitleBar(config.topBarHeight.value)
      window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
      window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
      window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
    }
    onDispose {}
  }

  if (!immersiveEnabled) {
    content()
    return
  }

  CompositionLocalProvider(
    LocalWorkbenchWindowFrame provides immersiveFrame,
  ) {
    content()
  }
}

private fun shouldEnableImmersiveTopBar(
  state: WindowState,
  toggleSystemPropertyKey: String?,
): Boolean {
  if (!isMacOs() || state.placement == WindowPlacement.Fullscreen) {
    return false
  }
  val explicitToggle = toggleSystemPropertyKey
    ?.takeIf(String::isNotBlank)
    ?.let(System::getProperty)
    ?.toBooleanStrictOrNull()
  if (explicitToggle != null) {
    return explicitToggle
  }
  return isMacImmersiveHostCompatible()
}

private fun isMacOs(): Boolean {
  return System.getProperty("os.name")
    ?.contains("Mac", ignoreCase = true) == true
}

private fun isMacImmersiveHostCompatible(): Boolean {
  val javaFeature = Runtime.version().feature()
  val macMajorVersion = System.getProperty("os.version")
    ?.substringBefore('.')
    ?.toIntOrNull()
  return javaFeature < 25 && (macMajorVersion == null || macMajorVersion < 26)
}

private fun <T : JComponent> findComponent(
  container: Container,
  klass: Class<T>,
): T? {
  val componentSequence = container.components.asSequence()
  return componentSequence.filter { klass.isInstance(it) }
    .ifEmpty {
      componentSequence.filterIsInstance<Container>()
        .mapNotNull { child -> findComponent(child, klass) }
    }
    .map { component -> klass.cast(component) }
    .firstOrNull()
}

private inline fun <reified T : JComponent> Container.findComponent(): T? {
  return findComponent(this, T::class.java)
}

private fun ComposeWindow.findSkiaLayer(): SkiaLayer? {
  return findComponent()
}
