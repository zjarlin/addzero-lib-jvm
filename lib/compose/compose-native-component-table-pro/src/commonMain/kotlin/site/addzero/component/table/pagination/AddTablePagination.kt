package site.addzero.component.table.pagination

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.addzero.component.card.MellumCardType
import site.addzero.component.dropdown.AddSelect
import site.addzero.component.dropdown.SelectMode
import site.addzero.component.table.original.entity.StatePagination

/**
 * 表格分页组件。
 *
 * 视觉上采用紧凑的后台工作台风格，而不是展示型大卡片。
 */
@Composable
fun AddTablePagination(
    modifier: Modifier = Modifier,
    statePagination: StatePagination,
    pageSizeOptions: List<Int> = listOf(10, 20, 50, 100),
    enablePagination: Boolean,
    onPageSizeChange: (Int) -> Unit,
    onGoFirstPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onGoToPage: (Int) -> Unit,
    onNextPage: () -> Unit,
    onGoLastPage: () -> Unit,
    cardType: MellumCardType = MellumCardType.Light,
    showPageSizeSelector: Boolean = true,
    showPageInfo: Boolean = true,
    compactMode: Boolean = false,
) {
    if (!enablePagination) {
        return
    }

    val containerColor = when (cardType) {
        MellumCardType.Dark -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f),
        ),
    ) {
        if (compactMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showPageInfo) {
                    PaginationInfo(
                        statePagination = statePagination,
                        compact = true,
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                PaginationControls(
                    statePagination = statePagination,
                    onGoFirstPage = onGoFirstPage,
                    onPreviousPage = onPreviousPage,
                    onGoToPage = onGoToPage,
                    onNextPage = onNextPage,
                    onGoLastPage = onGoLastPage,
                    compact = true,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showPageInfo) {
                    PaginationInfo(
                        statePagination = statePagination,
                        compact = false,
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                PaginationControls(
                    statePagination = statePagination,
                    onGoFirstPage = onGoFirstPage,
                    onPreviousPage = onPreviousPage,
                    onGoToPage = onGoToPage,
                    onNextPage = onNextPage,
                    onGoLastPage = onGoLastPage,
                    compact = false,
                )

                if (showPageSizeSelector) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "每页",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AddSelect(
                            modifier = Modifier.width(128.dp),
                            value = statePagination.pageSize,
                            items = pageSizeOptions,
                            onValueChange = onPageSizeChange,
                            placeholder = "${statePagination.pageSize} 条",
                            selectMode = SelectMode.SINGLE,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
    }
}

/**
 * 分页信息摘要。
 */
@Composable
private fun PaginationInfo(
    statePagination: StatePagination,
    compact: Boolean,
) {
    if (compact) {
        Text(
            text = "${statePagination.currentPage}/${statePagination.totalPages}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "第 ${statePagination.startItem}-${statePagination.endItem} 项，共 ${statePagination.totalItems} 项",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "第 ${statePagination.currentPage} 页，共 ${statePagination.totalPages} 页",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 分页控制按钮组。
 */
@Composable
private fun PaginationControls(
    statePagination: StatePagination,
    onGoFirstPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onGoToPage: (Int) -> Unit,
    onNextPage: () -> Unit,
    onGoLastPage: () -> Unit,
    compact: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaginationButton(
            onClick = onGoFirstPage,
            enabled = statePagination.currentPage > 1,
            compact = compact,
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "首页",
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
            )
        }
        PaginationButton(
            onClick = onPreviousPage,
            enabled = statePagination.currentPage > 1,
            compact = compact,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "上一页",
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
            )
        }

        if (!compact) {
            PageNumberButtons(
                currentPage = statePagination.currentPage,
                totalPages = statePagination.totalPages,
                onGoToPage = onGoToPage,
            )
        }

        PaginationButton(
            onClick = onNextPage,
            enabled = statePagination.currentPage < statePagination.totalPages,
            compact = compact,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "下一页",
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
            )
        }
        PaginationButton(
            onClick = onGoLastPage,
            enabled = statePagination.currentPage < statePagination.totalPages,
            compact = compact,
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "末页",
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
            )
        }
    }
}

/**
 * 分页按钮。
 */
@Composable
private fun PaginationButton(
    onClick: () -> Unit,
    enabled: Boolean,
    compact: Boolean,
    content: @Composable () -> Unit,
) {
    val size = if (compact) 32.dp else 36.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                color = if (enabled) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                },
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                color = if (enabled) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)
                },
                shape = CircleShape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            },
        ) {
            content()
        }
    }
}

/**
 * 页码按钮组。
 */
@Composable
private fun PageNumberButtons(
    currentPage: Int,
    totalPages: Int,
    onGoToPage: (Int) -> Unit,
) {
    val visiblePages = 5
    val halfVisible = visiblePages / 2
    val startPage = maxOf(1, currentPage - halfVisible)
    val endPage = minOf(totalPages, startPage + visiblePages - 1)

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (page in startPage..endPage) {
            val isCurrentPage = page == currentPage

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isCurrentPage) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isCurrentPage) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f)
                        },
                        shape = CircleShape,
                    )
                    .clickable { onGoToPage(page) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = page.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isCurrentPage) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        if (endPage < totalPages) {
            Text(
                text = "...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
