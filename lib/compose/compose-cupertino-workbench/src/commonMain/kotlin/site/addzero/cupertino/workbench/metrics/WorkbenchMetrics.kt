package site.addzero.cupertino.workbench.metrics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.component.tree.AddTreeDefaults
import site.addzero.component.tree.AddTreeMetrics

@Immutable
data class WorkbenchMetrics(
  val compact: Boolean = false,
  val topBarHeight: Dp = 56.dp,
  val topBarLeadingInset: Dp = 84.dp,
  val defaultWindowWidth: Dp = 1440.dp,
  val defaultWindowHeight: Dp = 920.dp,
  val sidebarRatio: Float = 0.19f,
  val sidebarMinWidth: Dp = 284.dp,
  val sidebarMaxWidth: Dp = 360.dp,
  val sidebarOuterPadding: PaddingValues = PaddingValues(start = 12.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
  val sidebarPanelRadius: Dp = 24.dp,
  val sidebarPanelInnerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
  val sidebarSectionGap: Dp = 12.dp,
  val sidebarTreePanelRadius: Dp = 18.dp,
  val sidebarTreePanelPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
  val headerPanelRadius: Dp = 24.dp,
  val headerPanelPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
  val headerSectionGap: Dp = 14.dp,
  val headerMetricRadius: Dp = 18.dp,
  val headerMetricPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
  val sceneTabHorizontalPadding: Dp = 14.dp,
  val sceneTabVerticalPadding: Dp = 9.dp,
  val searchFieldHeight: Dp = 56.dp,
  val searchFieldSpacing: Dp = 12.dp,
  val searchFieldCompactRefreshHidden: Boolean = false,
  val contentPanelRadius: Dp = 28.dp,
  val contentPanelPadding: PaddingValues = PaddingValues(start = 4.dp, end = 12.dp, bottom = 12.dp),
  val contentInnerPadding: Dp = 12.dp,
  val treeMetrics: AddTreeMetrics = AddTreeDefaults.AppleRoundedMetrics,
)

object WorkbenchPresets {
  val Comfortable = WorkbenchMetrics()

  val DesktopCompact = WorkbenchMetrics(
    compact = true,
    topBarHeight = 48.dp,
    topBarLeadingInset = 74.dp,
    defaultWindowWidth = 1560.dp,
    defaultWindowHeight = 960.dp,
    sidebarRatio = 0.17f,
    sidebarMinWidth = 252.dp,
    sidebarMaxWidth = 320.dp,
    sidebarOuterPadding = PaddingValues(start = 10.dp, top = 10.dp, end = 6.dp, bottom = 10.dp),
    sidebarPanelRadius = 20.dp,
    sidebarPanelInnerPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    sidebarSectionGap = 10.dp,
    sidebarTreePanelRadius = 14.dp,
    sidebarTreePanelPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
    headerPanelRadius = 18.dp,
    headerPanelPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    headerSectionGap = 10.dp,
    headerMetricRadius = 14.dp,
    headerMetricPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    sceneTabHorizontalPadding = 12.dp,
    sceneTabVerticalPadding = 7.dp,
    searchFieldHeight = 46.dp,
    searchFieldSpacing = 8.dp,
    searchFieldCompactRefreshHidden = true,
    contentPanelRadius = 22.dp,
    contentPanelPadding = PaddingValues(start = 2.dp, end = 10.dp, bottom = 10.dp),
    contentInnerPadding = 10.dp,
    treeMetrics = AddTreeDefaults.CompactAppleRoundedMetrics,
  )
}

typealias CupertinoWorkbenchPresets = WorkbenchPresets

val LocalWorkbenchMetrics = staticCompositionLocalOf { WorkbenchPresets.Comfortable }
val LocalCupertinoWorkbenchMetrics = LocalWorkbenchMetrics

@Composable
fun currentWorkbenchMetrics(): WorkbenchMetrics = LocalWorkbenchMetrics.current
