@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.cupertino.workbench.components.panel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme

@Composable
fun CupertinoPanel(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  actions: @Composable RowScope.() -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  CupertinoSurface(
    modifier = modifier
      .fillMaxWidth()
      .border(
        border = BorderStroke(1.dp, CupertinoTheme.colorScheme.separator.copy(alpha = 0.35f)),
        shape = CupertinoTheme.shapes.large,
      ),
    color = CupertinoTheme.colorScheme.secondarySystemGroupedBackground,
    shape = CupertinoTheme.shapes.large,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          CupertinoText(
            text = title,
            style = CupertinoTheme.typography.title3,
          )
          subtitle?.takeIf(String::isNotBlank)?.let { text ->
            CupertinoText(
              text = text,
              style = CupertinoTheme.typography.footnote,
              color = CupertinoTheme.colorScheme.secondaryLabel,
            )
          }
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          content = actions,
        )
      }
      content()
    }
  }
}

@Composable
fun CupertinoStatusStrip(
  text: String,
  modifier: Modifier = Modifier,
  tone: Color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = tone,
        shape = CupertinoTheme.shapes.medium,
      )
      .padding(horizontal = 12.dp, vertical = 10.dp),
  ) {
    CupertinoText(
      text = text,
      style = CupertinoTheme.typography.footnote,
    )
  }
}

@Composable
fun CupertinoKeyValueRow(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CupertinoText(
      text = label,
      color = CupertinoTheme.colorScheme.secondaryLabel,
    )
    CupertinoText(
      text = value,
    )
  }
}

@Composable
fun CupertinoSectionTitle(
  text: String,
) {
  CupertinoText(
    text = text,
    style = CupertinoTheme.typography.headline,
  )
}
