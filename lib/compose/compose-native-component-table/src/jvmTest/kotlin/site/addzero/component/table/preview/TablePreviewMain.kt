package site.addzero.component.table.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import site.addzero.component.table.original.TableOriginal
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.TableLayoutConfig

/**
 * 表格组件桌面预览入口。
 *
 * 该入口位于 `jvmTest`，只用于本地开发观察完整窗口效果，
 * 不会进入正式发布的库产物。
 */
fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "AddZero Table Preview",
  ) {
    MaterialTheme {
      TablePreviewApp()
    }
  }
}

/**
 * 渲染表格预览页面。
 */
@Composable
private fun TablePreviewApp() {
  val rows = remember { previewRows() }
  val columns = remember { TablePreviewColumn.entries }
  val columnConfigs = remember { previewColumnConfigs() }
  val layoutConfig = remember {
    TableLayoutConfig(
      indexColumnWidthDp = 64f,
      leftSlotWidthDp = 96f,
      actionColumnWidthDp = 176f,
      headerHeightDp = 60f,
      rowHeightDp = 64f,
      defaultColumnWidthDp = 148f,
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFFF6F8FC),
            Color(0xFFEEF3FB),
          ),
        ),
      )
      .padding(24.dp),
  ) {
    TableOriginal(
      data = rows,
      columns = columns,
      getColumnKey = { column -> column.key },
      getRowId = { row -> row.id },
      columnConfigs = columnConfigs,
      layoutConfig = layoutConfig,
      topSlot = {
        PreviewTopBar(
          rowCount = rows.size,
        )
      },
      bottomSlot = {
        PreviewBottomBar()
      },
      rowLeftSlot = { item, _ ->
        SeverityBadge(level = item.severity)
      },
      rowActionSlot = { item ->
        PreviewActionCell(status = item.status)
      },
      getCellContent = { item, column ->
        PreviewCell(
          row = item,
          column = column,
        )
      },
      modifier = Modifier.fillMaxSize(),
    )
  }
}

/**
 * 预览顶部摘要区域。
 */
@Composable
private fun PreviewTopBar(
  rowCount: Int,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = "运营看板表格预览",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      PreviewSummaryCard(
        title = "活跃服务",
        value = rowCount.toString(),
        accent = Color(0xFF2F6BFF),
      )
      PreviewSummaryCard(
        title = "高风险条目",
        value = "4",
        accent = Color(0xFFE66A4E),
      )
      PreviewSummaryCard(
        title = "平均延迟",
        value = "214ms",
        accent = Color(0xFF1B9C73),
      )
    }
  }
}

/**
 * 预览底部说明条。
 */
@Composable
private fun PreviewBottomBar() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 14.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "当前预览聚焦固定列、长表头、横向滚动和密集后台视觉。",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = "运行命令: ./gradlew :lib:compose:compose-native-component-table:previewTable",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
    )
  }
}

/**
 * 摘要信息卡片。
 */
@Composable
private fun PreviewSummaryCard(
  title: String,
  value: String,
  accent: Color,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color.White.copy(alpha = 0.88f),
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        color = accent,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

/**
 * 左侧严重度标记。
 */
@Composable
private fun SeverityBadge(
  level: String,
) {
  val accent = when (level) {
    "高" -> Color(0xFFE66A4E)
    "中" -> Color(0xFFF4B740)
    else -> Color(0xFF4C8BF5)
  }

  Surface(
    modifier = Modifier.width(80.dp),
    shape = RoundedCornerShape(999.dp),
    color = accent.copy(alpha = 0.14f),
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "$level 优先级",
        style = MaterialTheme.typography.labelMedium,
        color = accent,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
      )
    }
  }
}

/**
 * 操作列示例。
 */
@Composable
private fun PreviewActionCell(
  status: String,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    SmallActionPill(
      label = "查看",
      background = Color(0xFFEDF4FF),
      textColor = Color(0xFF2F6BFF),
    )
    SmallActionPill(
      label = if (status == "异常") "处置" else "详情",
      background = Color(0xFFF6F0FF),
      textColor = Color(0xFF7B4DCC),
    )
  }
}

/**
 * 行内动作胶囊。
 */
@Composable
private fun SmallActionPill(
  label: String,
  background: Color,
  textColor: Color,
) {
  Surface(
    shape = RoundedCornerShape(999.dp),
    color = background,
  ) {
    Text(
      text = label,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
      style = MaterialTheme.typography.labelMedium,
      color = textColor,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

/**
 * 预览单元格内容。
 */
@Composable
private fun PreviewCell(
  row: TablePreviewRow,
  column: TablePreviewColumn,
) {
  when (column) {
    TablePreviewColumn.PROJECT -> {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = row.project,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = row.owner,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    TablePreviewColumn.ENVIRONMENT -> PreviewMutedText(row.environment)
    TablePreviewColumn.STATUS -> StatusBadge(row.status)
    TablePreviewColumn.LATENCY -> MetricText("${row.latencyMs} ms")
    TablePreviewColumn.ERROR_RATE -> MetricText(row.errorRate)
    TablePreviewColumn.TRAFFIC -> MetricText(row.traffic)
    TablePreviewColumn.REGION -> PreviewMutedText(row.region)
    TablePreviewColumn.UPDATED_AT -> PreviewMutedText(row.updatedAt)
    TablePreviewColumn.OWNER -> PreviewMutedText(row.owner)
  }
}

/**
 * 状态徽标。
 */
@Composable
private fun StatusBadge(
  status: String,
) {
  val color = when (status) {
    "正常" -> Color(0xFF1B9C73)
    "关注" -> Color(0xFFF4B740)
    else -> Color(0xFFE66A4E)
  }

  Surface(
    shape = RoundedCornerShape(999.dp),
    color = color.copy(alpha = 0.14f),
  ) {
    Text(
      text = status,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
      style = MaterialTheme.typography.labelMedium,
      color = color,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

/**
 * 指标值文本。
 */
@Composable
private fun MetricText(
  value: String,
) {
  Text(
    text = value,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurface,
    fontWeight = FontWeight.Medium,
  )
}

/**
 * 次级文本样式。
 */
@Composable
private fun PreviewMutedText(
  value: String,
) {
  Text(
    text = value,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
  )
}

/**
 * 预览列定义。
 */
private enum class TablePreviewColumn(
  val key: String,
) {
  PROJECT("project"),
  OWNER("owner"),
  ENVIRONMENT("environment"),
  STATUS("status"),
  LATENCY("latencyMs"),
  ERROR_RATE("errorRate"),
  TRAFFIC("traffic"),
  REGION("region"),
  UPDATED_AT("updatedAt"),
}

/**
 * 预览数据行。
 */
private data class TablePreviewRow(
  val id: Long,
  val project: String,
  val owner: String,
  val environment: String,
  val status: String,
  val severity: String,
  val latencyMs: Int,
  val errorRate: String,
  val traffic: String,
  val region: String,
  val updatedAt: String,
)

/**
 * 生成列配置。
 */
private fun previewColumnConfigs(): List<ColumnConfig> {
  return listOf(
    ColumnConfig(key = "project", comment = "服务 / 项目", width = 220f, order = 0),
    ColumnConfig(key = "owner", comment = "负责人", width = 140f, order = 1),
    ColumnConfig(key = "environment", comment = "环境", width = 120f, order = 2),
    ColumnConfig(key = "status", comment = "运行状态", width = 120f, order = 3),
    ColumnConfig(key = "latencyMs", comment = "平均延迟", width = 120f, order = 4),
    ColumnConfig(key = "errorRate", comment = "错误率", width = 120f, order = 5),
    ColumnConfig(key = "traffic", comment = "24h 流量", width = 120f, order = 6),
    ColumnConfig(key = "region", comment = "区域", width = 140f, order = 7),
    ColumnConfig(key = "updatedAt", comment = "最近更新时间", width = 180f, order = 8),
  )
}

/**
 * 生成桌面预览样例数据。
 */
private fun previewRows(): List<TablePreviewRow> {
  return listOf(
    TablePreviewRow(1, "gateway-public-api", "Luna", "生产", "正常", "低", 102, "0.08%", "1.8M", "华东 1", "2026-04-03 18:20"),
    TablePreviewRow(2, "auth-session-center", "Kai", "生产", "关注", "中", 236, "0.34%", "860K", "华东 2", "2026-04-03 18:18"),
    TablePreviewRow(3, "billing-invoice-worker", "Mia", "预发", "异常", "高", 412, "1.82%", "420K", "华北 1", "2026-04-03 18:11"),
    TablePreviewRow(4, "device-shadow-sync", "Noah", "生产", "正常", "低", 148, "0.15%", "3.2M", "华南 1", "2026-04-03 18:16"),
    TablePreviewRow(5, "tenant-config-service", "Emma", "预发", "关注", "中", 269, "0.48%", "610K", "新加坡", "2026-04-03 18:09"),
    TablePreviewRow(6, "workflow-orchestrator", "Liam", "生产", "异常", "高", 533, "2.46%", "1.1M", "华东 1", "2026-04-03 18:05"),
    TablePreviewRow(7, "media-transcode-core", "Ava", "生产", "正常", "低", 176, "0.09%", "2.4M", "华南 1", "2026-04-03 17:59"),
    TablePreviewRow(8, "ops-notify-center", "Ethan", "测试", "关注", "中", 221, "0.62%", "290K", "华北 1", "2026-04-03 17:48"),
    TablePreviewRow(9, "plugin-market-indexer", "Sophia", "生产", "正常", "低", 164, "0.12%", "1.3M", "华东 2", "2026-04-03 17:42"),
    TablePreviewRow(10, "audit-log-archive", "Leo", "生产", "关注", "中", 304, "0.71%", "970K", "香港", "2026-04-03 17:40"),
    TablePreviewRow(11, "scene-rule-engine", "Olivia", "预发", "正常", "低", 188, "0.11%", "740K", "华东 1", "2026-04-03 17:36"),
    TablePreviewRow(12, "edge-telemetry-stream", "Mason", "生产", "异常", "高", 614, "3.18%", "4.7M", "法兰克福", "2026-04-03 17:31"),
  )
}
