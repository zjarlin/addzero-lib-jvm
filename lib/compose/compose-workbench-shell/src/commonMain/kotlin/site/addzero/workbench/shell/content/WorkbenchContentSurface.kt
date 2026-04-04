package site.addzero.workbench.shell.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.workbench.shell.metrics.currentWorkbenchMetrics

@Composable
fun WorkbenchContentSurface(
    modifier: Modifier = Modifier,
    panelPadding: PaddingValues = currentWorkbenchMetrics().contentPanelPadding,
    innerPadding: Dp = currentWorkbenchMetrics().contentInnerPadding,
    content: @Composable BoxScope.() -> Unit,
) {
    val metrics = currentWorkbenchMetrics()
    Surface(
        modifier = modifier.padding(panelPadding),
        shape = RoundedCornerShape(metrics.contentPanelRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            content = content,
        )
    }
}
