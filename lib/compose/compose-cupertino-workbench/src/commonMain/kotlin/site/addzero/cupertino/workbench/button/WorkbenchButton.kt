package site.addzero.cupertino.workbench.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.robinpcrd.cupertino.CupertinoButton
import io.github.robinpcrd.cupertino.CupertinoButtonColors
import io.github.robinpcrd.cupertino.CupertinoButtonDefaults
import io.github.robinpcrd.cupertino.CupertinoButtonSize
import io.github.robinpcrd.cupertino.ExperimentalCupertinoApi
import site.addzero.cupertino.workbench.material3.Icon
import site.addzero.cupertino.workbench.material3.MaterialTheme
import site.addzero.cupertino.workbench.material3.Text

enum class WorkbenchButtonVariant {
  Default,
  Destructive,
  Outline,
  Secondary,
  Ghost,
  Link,
}

enum class WorkbenchButtonSize {
  Default,
  Sm,
  Lg,
  Icon,
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun WorkbenchButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Default,
  size: WorkbenchButtonSize = WorkbenchButtonSize.Default,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(16.dp),
  content: @Composable RowScope.() -> Unit,
) {
  val colors = workbenchButtonColors(
    variant = variant,
    enabled = enabled,
  )
  CupertinoButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    size = size.toCupertinoSize(),
    colors = colors,
    shape = shape,
    border = variant.toBorder(),
    content = content,
  )
}

@Composable
fun WorkbenchPillButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Default,
  size: WorkbenchButtonSize = WorkbenchButtonSize.Default,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  WorkbenchButton(
    onClick = onClick,
    modifier = modifier,
    variant = variant,
    size = size,
    enabled = enabled,
    shape = RoundedCornerShape(999.dp),
    content = content,
  )
}

@Composable
fun WorkbenchOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(16.dp),
  content: @Composable RowScope.() -> Unit,
) {
  WorkbenchButton(
    onClick = onClick,
    modifier = modifier,
    variant = WorkbenchButtonVariant.Outline,
    enabled = enabled,
    shape = shape,
    content = content,
  )
}

@Composable
fun WorkbenchFilledTonalButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(16.dp),
  content: @Composable RowScope.() -> Unit,
) {
  WorkbenchButton(
    onClick = onClick,
    modifier = modifier,
    variant = WorkbenchButtonVariant.Secondary,
    enabled = enabled,
    shape = shape,
    content = content,
  )
}

@Composable
fun WorkbenchTextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  WorkbenchButton(
    onClick = onClick,
    modifier = modifier,
    variant = WorkbenchButtonVariant.Ghost,
    enabled = enabled,
    content = content,
  )
}

@Composable
fun WorkbenchActionButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  imageVector: ImageVector? = null,
  variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Default,
  size: WorkbenchButtonSize = WorkbenchButtonSize.Default,
  enabled: Boolean = true,
  pill: Boolean = false,
) {
  val content: @Composable RowScope.() -> Unit = {
    imageVector?.let { icon ->
      Icon(
        imageVector = icon,
        contentDescription = null,
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(text)
  }
  if (pill) {
    WorkbenchPillButton(
      onClick = onClick,
      modifier = modifier,
      variant = variant,
      size = size,
      enabled = enabled,
      content = content,
    )
  } else {
    WorkbenchButton(
      onClick = onClick,
      modifier = modifier,
      variant = variant,
      size = size,
      enabled = enabled,
      content = content,
    )
  }
}

@Composable
fun WorkbenchIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tooltip: String? = null,
  variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Ghost,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  WorkbenchPillButton(
    onClick = onClick,
    modifier = modifier,
    variant = variant,
    size = WorkbenchButtonSize.Icon,
    enabled = enabled,
    content = content,
  )
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
@ReadOnlyComposable
private fun workbenchButtonColors(
  variant: WorkbenchButtonVariant,
  enabled: Boolean,
): CupertinoButtonColors {
  val colors = MaterialTheme.colorScheme
  return when (variant) {
    WorkbenchButtonVariant.Default -> CupertinoButtonDefaults.filledButtonColors(
      containerColor = colors.primary,
      contentColor = colors.onPrimary,
    )

    WorkbenchButtonVariant.Destructive -> CupertinoButtonDefaults.filledButtonColors(
      containerColor = colors.error,
      contentColor = colors.onError,
    )

    WorkbenchButtonVariant.Outline -> CupertinoButtonDefaults.tintedButtonColors(
      contentColor = colors.primary,
      containerColor = colors.primary.copy(alpha = 0.10f),
    )

    WorkbenchButtonVariant.Secondary -> CupertinoButtonDefaults.grayButtonColors(
      contentColor = colors.onSurface,
      containerColor = colors.surfaceVariant,
    )

    WorkbenchButtonVariant.Ghost -> CupertinoButtonDefaults.plainButtonColors(
      contentColor = colors.onSurface,
    )

    WorkbenchButtonVariant.Link -> CupertinoButtonDefaults.plainButtonColors(
      contentColor = colors.primary,
    )
  }
}

@Composable
private fun WorkbenchButtonVariant.toBorder(): BorderStroke? {
  val colors = MaterialTheme.colorScheme
  return when (this) {
    WorkbenchButtonVariant.Outline -> BorderStroke(1.dp, colors.outlineVariant)
    else -> null
  }
}

private fun WorkbenchButtonSize.toCupertinoSize(): CupertinoButtonSize {
  return when (this) {
    WorkbenchButtonSize.Default -> CupertinoButtonSize.Regular
    WorkbenchButtonSize.Sm -> CupertinoButtonSize.Small
    WorkbenchButtonSize.Lg -> CupertinoButtonSize.Large
    WorkbenchButtonSize.Icon -> CupertinoButtonSize.Small
  }
}
