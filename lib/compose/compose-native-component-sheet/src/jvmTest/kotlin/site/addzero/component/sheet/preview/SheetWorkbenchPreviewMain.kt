package site.addzero.component.sheet.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import site.addzero.component.sheet.engine.SheetCellAddress
import site.addzero.component.sheet.engine.SheetCellValue
import site.addzero.component.sheet.engine.SheetDataSource
import site.addzero.component.sheet.engine.SheetDocument
import site.addzero.component.sheet.engine.SheetOperation
import site.addzero.component.sheet.engine.SheetPage
import site.addzero.component.sheet.engine.SheetReducer
import site.addzero.component.sheet.engine.rememberSheetController
import site.addzero.component.sheet.ui.SheetWorkbench

/**
 * 在线表格工作台桌面预览入口。
 *
 * 只放在 `jvmTest`，用于独立手工验证。
 */
fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "AddZero Sheet Preview",
  ) {
    val autoExitMillis = System.getProperty("sheet.preview.autoExitMillis")
      ?.toLongOrNull()
      ?.takeIf { it > 0L }
    if (autoExitMillis != null) {
      LaunchedEffect(autoExitMillis) {
        delay(autoExitMillis)
        exitApplication()
      }
    }

    MaterialTheme {
      SheetWorkbenchPreviewApp()
    }
  }
}

@Composable
private fun SheetWorkbenchPreviewApp() {
  val dataSource = remember { SheetPreviewDataSource() }
  val controller = rememberSheetController(
    dataSource = dataSource,
    documentId = "sheet-preview",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Color(0xFFF6F8FC),
            Color(0xFFEAF1FB),
          ),
        ),
      )
      .padding(20.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      PreviewHeader()
      Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
        color = Color.White.copy(alpha = 0.94f),
      ) {
        SheetWorkbench(
          controller = controller,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun PreviewHeader() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = "在线表格工作台预览",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "支持单元格编辑、拖拽框选、范围填充和插删行列。",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Text(
      text = "命令: :lib:compose:compose-native-component-sheet:previewSheetWorkbench",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
    )
  }
}

private class SheetPreviewDataSource : SheetDataSource {
  private var document = buildPreviewDocument()

  override suspend fun load(documentId: String): SheetDocument {
    check(documentId == document.documentId) {
      "未找到文档: $documentId"
    }
    return document
  }

  override suspend fun applyOperations(
    documentId: String,
    baseVersion: Long,
    operations: List<SheetOperation>,
  ): SheetDocument {
    check(documentId == document.documentId) {
      "未找到文档: $documentId"
    }
    check(document.version == baseVersion) {
      "版本冲突: current=${document.version}, base=$baseVersion"
    }
    document = SheetReducer.apply(document, operations)
    return document
  }
}

private fun buildPreviewDocument(): SheetDocument {
  return SheetDocument(
    documentId = "sheet-preview",
    activeSheetId = "config-meta",
    sheets = listOf(
      SheetPage(
        sheetId = "config-meta",
        title = "配置元数据",
        rowCount = 48,
        columnCount = 8,
        cells = mapOf(
          SheetCellAddress(0, 0) to SheetCellValue.infer("key"),
          SheetCellAddress(0, 1) to SheetCellValue.infer("value"),
          SheetCellAddress(0, 2) to SheetCellValue.infer("type"),
          SheetCellAddress(0, 3) to SheetCellValue.infer("comment"),
          SheetCellAddress(1, 0) to SheetCellValue.infer("jdbc.url"),
          SheetCellAddress(1, 1) to SheetCellValue.infer("jdbc:postgresql://127.0.0.1:5432/kcloud"),
          SheetCellAddress(1, 2) to SheetCellValue.infer("TEXT"),
          SheetCellAddress(1, 3) to SheetCellValue.infer("主数据源 JDBC 连接"),
          SheetCellAddress(2, 0) to SheetCellValue.infer("jdbc.auto-ddl"),
          SheetCellAddress(2, 1) to SheetCellValue.infer("false"),
          SheetCellAddress(2, 2) to SheetCellValue.infer("BOOLEAN"),
          SheetCellAddress(2, 3) to SheetCellValue.infer("不存在库表时自动建表开关"),
          SheetCellAddress(3, 0) to SheetCellValue.infer("iot.modbus.timeout-ms"),
          SheetCellAddress(3, 1) to SheetCellValue.infer("3000"),
          SheetCellAddress(3, 2) to SheetCellValue.infer("NUMBER"),
          SheetCellAddress(3, 3) to SheetCellValue.infer("Modbus 通讯超时"),
        ),
      ),
      SheetPage(
        sheetId = "device-env",
        title = "设备环境",
        rowCount = 32,
        columnCount = 6,
        cells = mapOf(
          SheetCellAddress(0, 0) to SheetCellValue.infer("device-id"),
          SheetCellAddress(0, 1) to SheetCellValue.infer("env"),
          SheetCellAddress(0, 2) to SheetCellValue.infer("ip"),
          SheetCellAddress(1, 0) to SheetCellValue.infer("plc-01"),
          SheetCellAddress(1, 1) to SheetCellValue.infer("prod"),
          SheetCellAddress(1, 2) to SheetCellValue.infer("10.10.1.15"),
          SheetCellAddress(2, 0) to SheetCellValue.infer("plc-02"),
          SheetCellAddress(2, 1) to SheetCellValue.infer("staging"),
          SheetCellAddress(2, 2) to SheetCellValue.infer("10.10.2.21"),
        ),
      ),
    ),
  )
}
