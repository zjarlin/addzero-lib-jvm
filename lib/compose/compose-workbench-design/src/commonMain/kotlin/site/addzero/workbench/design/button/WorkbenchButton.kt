package site.addzero.workbench.design.button

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import site.addzero.component.Button as ShadcnButton
import site.addzero.component.ButtonSize as ShadcnButtonSize
import site.addzero.component.ButtonVariant as ShadcnButtonVariant
import site.addzero.themes.radius

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

@Composable
fun WorkbenchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Default,
    size: WorkbenchButtonSize = WorkbenchButtonSize.Default,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(MaterialTheme.radius.md),
    content: @Composable RowScope.() -> Unit,
) {
    ShadcnButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant.toShadcnVariant(),
        size = size.toShadcnSize(),
        enabled = enabled,
        shape = shape,
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
    shape: Shape = RoundedCornerShape(MaterialTheme.radius.md),
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
    shape: Shape = RoundedCornerShape(MaterialTheme.radius.md),
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
    val buttonContent: @Composable RowScope.() -> Unit = {
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
            content = buttonContent,
        )
    } else {
        WorkbenchButton(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = size,
            enabled = enabled,
            content = buttonContent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkbenchIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    variant: WorkbenchButtonVariant = WorkbenchButtonVariant.Ghost,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val anchor: @Composable () -> Unit = {
        WorkbenchPillButton(
            onClick = onClick,
            modifier = modifier,
            variant = variant,
            size = WorkbenchButtonSize.Icon,
            enabled = enabled,
            content = content,
        )
    }

    if (tooltip.isNullOrBlank()) {
        anchor()
    } else {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above,
            ),
            tooltip = {
                PlainTooltip {
                    Text(tooltip)
                }
            },
            state = rememberTooltipState(),
        ) {
            anchor()
        }
    }
}

private fun WorkbenchButtonVariant.toShadcnVariant(): ShadcnButtonVariant =
    when (this) {
        WorkbenchButtonVariant.Default -> ShadcnButtonVariant.Default
        WorkbenchButtonVariant.Destructive -> ShadcnButtonVariant.Destructive
        WorkbenchButtonVariant.Outline -> ShadcnButtonVariant.Outline
        WorkbenchButtonVariant.Secondary -> ShadcnButtonVariant.Secondary
        WorkbenchButtonVariant.Ghost -> ShadcnButtonVariant.Ghost
        WorkbenchButtonVariant.Link -> ShadcnButtonVariant.Link
    }

private fun WorkbenchButtonSize.toShadcnSize(): ShadcnButtonSize =
    when (this) {
        WorkbenchButtonSize.Default -> ShadcnButtonSize.Default
        WorkbenchButtonSize.Sm -> ShadcnButtonSize.Sm
        WorkbenchButtonSize.Lg -> ShadcnButtonSize.Lg
        WorkbenchButtonSize.Icon -> ShadcnButtonSize.Icon
    }
