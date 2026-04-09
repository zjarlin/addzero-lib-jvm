package site.addzero.cupertino.workbench.scaffolding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.icons.CupertinoIcons
import io.github.robinpcrd.cupertino.icons.outlined.SidebarLeft
import site.addzero.appsidebar.LocalWorkbenchWindowFrame
import site.addzero.appsidebar.WorkbenchScaffold
import site.addzero.appsidebar.spi.scaffoldConfig
import site.addzero.appsidebar.spi.sidebarResizeConfig
import site.addzero.appsidebar.workbenchScaffoldDecor
import site.addzero.cupertino.workbench.button.WorkbenchButtonVariant
import site.addzero.cupertino.workbench.button.WorkbenchIconButton
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Surface
import site.addzero.cupertino.workbench.metrics.currentWorkbenchMetrics
import site.addzero.workbenchshell.spi.scaffolding.ScaffoldingSpi

@Composable
fun RenderCupertinoWorkbenchScaffolding(
  scaffolding: ScaffoldingSpi,
  modifier: Modifier = Modifier,
  sidebarVisible: Boolean = true,
  sidebarMode: CupertinoWorkbenchSidebarMode = if (sidebarVisible) {
    CupertinoWorkbenchSidebarMode.Expanded
  } else {
    CupertinoWorkbenchSidebarMode.Collapsed
  },
  onSidebarToggle: (() -> Unit)? = null,
  defaultSidebarRatio: Float = currentWorkbenchMetrics().sidebarRatio,
  minSidebarWidth: Dp = currentWorkbenchMetrics().sidebarMinWidth,
  maxSidebarWidth: Dp = currentWorkbenchMetrics().sidebarMaxWidth,
) {
  val metrics = currentWorkbenchMetrics()
  val resolvedSidebarMode = sidebarMode
  val windowFrame = LocalWorkbenchWindowFrame.current
  val topBarHeight = if (windowFrame.immersiveTopBar) {
    windowFrame.topBarHeight
  } else {
    metrics.topBarHeight
  }
  val resolvedSidebarRatio = if (resolvedSidebarMode == CupertinoWorkbenchSidebarMode.Collapsed) {
    0f
  } else {
    defaultSidebarRatio
  }
  val resolvedMinSidebarWidth = if (resolvedSidebarMode == CupertinoWorkbenchSidebarMode.Collapsed) {
    CupertinoWorkbenchCollapsedSidebarWidth
  } else {
    minSidebarWidth
  }
  val resolvedMaxSidebarWidth = if (resolvedSidebarMode == CupertinoWorkbenchSidebarMode.Collapsed) {
    CupertinoWorkbenchCollapsedSidebarWidth
  } else {
    maxSidebarWidth
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 10.dp),
        shape = RoundedCornerShape(metrics.headerPanelRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .padding(
              start = windowFrame.leadingInset + 16.dp,
              top = 10.dp,
              end = windowFrame.trailingInset + 16.dp,
              bottom = 10.dp,
            ),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (onSidebarToggle != null) {
            WorkbenchIconButton(
              onClick = onSidebarToggle,
              tooltip = if (resolvedSidebarMode == CupertinoWorkbenchSidebarMode.Collapsed) {
                "展开侧边栏"
              } else {
                "折叠侧边栏"
              },
              variant = if (resolvedSidebarMode == CupertinoWorkbenchSidebarMode.Collapsed) {
                WorkbenchButtonVariant.Outline
              } else {
                WorkbenchButtonVariant.Secondary
              },
            ) {
              Icon(
                imageVector = CupertinoIcons.Outlined.SidebarLeft,
                contentDescription = null,
              )
            }
          }
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            with(scaffolding) {
              RenderBrand()
              RenderHeader(
                modifier = Modifier.weight(1f),
              )
            }
          }
          with(scaffolding) {
            RenderTopBarActions()
          }
        }
      }

      WorkbenchScaffold(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        sidebar = {
          CompositionLocalProvider(
            LocalCupertinoWorkbenchSidebarMode provides resolvedSidebarMode,
          ) {
            scaffolding.RenderSidebar(
              modifier = Modifier.fillMaxSize(),
            )
          }
        },
        content = {
          scaffolding.RenderContent(
            modifier = Modifier.fillMaxSize(),
          )
        },
        config = scaffoldConfig(
          defaultSidebarRatio = resolvedSidebarRatio,
          minSidebarWidth = resolvedMinSidebarWidth,
          maxSidebarWidth = resolvedMaxSidebarWidth,
        ),
        decor = workbenchScaffoldDecor(
          sidebarContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          mainContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          headerContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          detailContainerModifier = Modifier.background(MaterialTheme.colorScheme.background),
          resizeConfig = sidebarResizeConfig(
            dividerColor = MaterialTheme.colorScheme.outlineVariant,
            thumbColor = MaterialTheme.colorScheme.surfaceVariant,
            thumbBorderColor = MaterialTheme.colorScheme.outline,
          ),
        ),
      )
    }
  }
}
