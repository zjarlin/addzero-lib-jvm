@file:OptIn(ExperimentalCupertinoApi::class)

package site.addzero.cupertino.workbench.components.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoSurface
import io.github.robinpcrd.cupertino.CupertinoText
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import io.github.robinpcrd.cupertino.theme.CupertinoTheme

enum class CupertinoFormSpan {
  HALF,
  FULL,
}

internal data class CupertinoFormItem(
  val span: CupertinoFormSpan,
  val content: @Composable () -> Unit,
)

class CupertinoFormGridScope internal constructor() {
  internal val items = mutableListOf<CupertinoFormItem>()

  fun item(
    span: CupertinoFormSpan = CupertinoFormSpan.HALF,
    content: @Composable () -> Unit,
  ) {
    items += CupertinoFormItem(
      span = span,
      content = content,
    )
  }

  fun fullWidth(
    content: @Composable () -> Unit,
  ) {
    item(
      span = CupertinoFormSpan.FULL,
      content = content,
    )
  }
}

@Composable
fun CupertinoFormGrid(
  modifier: Modifier = Modifier,
  twoColumnMinWidth: Dp = 640.dp,
  horizontalGap: Dp = 12.dp,
  verticalGap: Dp = 12.dp,
  content: CupertinoFormGridScope.() -> Unit,
) {
  val items = CupertinoFormGridScope().apply(content).items.toList()

  BoxWithConstraints(
    modifier = modifier.fillMaxWidth(),
  ) {
    val singleColumn = maxWidth < twoColumnMinWidth

    Column(
      verticalArrangement = Arrangement.spacedBy(verticalGap),
    ) {
      if (singleColumn) {
        items.forEach { item ->
          item.content()
        }
      } else {
        var pendingHalf: CupertinoFormItem? = null
        items.forEach { item ->
          when (item.span) {
            CupertinoFormSpan.FULL -> {
              pendingHalf?.let { left ->
                CupertinoFormHalfRow(
                  left = left,
                  right = null,
                  horizontalGap = horizontalGap,
                )
                pendingHalf = null
              }
              item.content()
            }

            CupertinoFormSpan.HALF -> {
              val left = pendingHalf
              if (left == null) {
                pendingHalf = item
              } else {
                CupertinoFormHalfRow(
                  left = left,
                  right = item,
                  horizontalGap = horizontalGap,
                )
                pendingHalf = null
              }
            }
          }
        }
        pendingHalf?.let { left ->
          CupertinoFormHalfRow(
            left = left,
            right = null,
            horizontalGap = horizontalGap,
          )
        }
      }
    }
  }
}

@Composable
private fun CupertinoFormHalfRow(
  left: CupertinoFormItem,
  right: CupertinoFormItem?,
  horizontalGap: Dp,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(horizontalGap),
    verticalAlignment = Alignment.Top,
  ) {
    Box(
      modifier = Modifier.weight(1f),
    ) {
      left.content()
    }
    if (right == null) {
      Spacer(modifier = Modifier.weight(1f))
    } else {
      Box(
        modifier = Modifier.weight(1f),
      ) {
        right.content()
      }
    }
  }
}

@Composable
fun CupertinoFormSection(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  twoColumnMinWidth: Dp = 640.dp,
  content: CupertinoFormGridScope.() -> Unit,
) {
  CupertinoSurface(
    modifier = modifier
      .fillMaxWidth()
      .border(
        border = BorderStroke(1.dp, CupertinoTheme.colorScheme.separator.copy(alpha = 0.22f)),
        shape = CupertinoTheme.shapes.large,
      ),
    color = CupertinoTheme.colorScheme.tertiarySystemGroupedBackground,
    shape = CupertinoTheme.shapes.large,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        CupertinoText(
          text = title,
          style = CupertinoTheme.typography.headline,
        )
        subtitle?.takeIf(String::isNotBlank)?.let { text ->
          CupertinoText(
            text = text,
            style = CupertinoTheme.typography.footnote,
            color = CupertinoTheme.colorScheme.secondaryLabel,
          )
        }
      }
      CupertinoFormGrid(
        twoColumnMinWidth = twoColumnMinWidth,
        content = content,
      )
    }
  }
}
