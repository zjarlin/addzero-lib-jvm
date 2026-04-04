package site.addzero.example.sheet

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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import site.addzero.component.sheet.engine.SheetCellAddress
import site.addzero.component.sheet.engine.SheetCellValue
import site.addzero.component.sheet.engine.SheetController
import site.addzero.component.sheet.engine.SheetDataSource
import site.addzero.component.sheet.engine.SheetDocument
import site.addzero.component.sheet.engine.SheetOperation
import site.addzero.component.sheet.engine.SheetPage
import site.addzero.component.sheet.engine.SheetRange
import site.addzero.component.sheet.engine.SheetReducer
import site.addzero.component.sheet.engine.SheetSelectionMode
import site.addzero.component.sheet.engine.createSheetController
import site.addzero.component.sheet.engine.rememberSheetController
import site.addzero.component.sheet.ui.SheetWorkbench
import kotlin.coroutines.coroutineContext

fun main(args: Array<String>) {
    if (args.firstOrNull() == "engine") {
        runBlocking { runEngineScenario() }
        return
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AddZero Sheet Example",
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
                SheetWorkbenchExampleApp()
            }
        }
    }
}

private suspend fun runEngineScenario() {
    println("== Example Sheet Engine Scenario ==")
    val dataSource = ExampleSheetDataSource(
        initialDocument = buildEngineDocument(),
    )
    val controller = createSheetController(
        scope = kotlinx.coroutines.CoroutineScope(coroutineContext),
        dataSource = dataSource,
        resolveLoadErrorMessage = { it.message ?: "加载失败" },
        resolveSaveErrorMessage = { it.message ?: "保存失败" },
    )

    controller.load("example-sheet-engine")
    settle(controller)
    printCheckpoint("加载完成", controller)

    controller.selectRange(
        range = SheetRange(
            start = SheetCellAddress(0, 0),
            end = SheetCellAddress(2, 1),
        ),
        mode = SheetSelectionMode.RANGE,
    )
    controller.fillDownSelection()
    settle(controller)
    printCheckpoint("向下填充后", controller)

    controller.selectColumn(1)
    controller.insertColumnsRight()
    settle(controller)
    printCheckpoint("右插列后", controller)

    controller.selectRange(
        range = SheetRange.single(SheetCellAddress(1, 2)),
        mode = SheetSelectionMode.CELL,
    )
    controller.pastePlainText("new-col\ttrue\nnext\t1")
    settle(controller)
    printCheckpoint("批量粘贴后", controller)

    val exportedSelection = controller.selectedRangeAsPlainText()
    println("导出选区：${exportedSelection.orEmpty()}")

    require(controller.activeSheet?.columnCount == 5) {
        "列数不符合预期: ${controller.activeSheet?.columnCount}"
    }
    require(controller.activeSheet?.cell(SheetCellAddress(1, 2))?.raw == "new-col") {
        "目标单元格内容不符合预期"
    }
    require(exportedSelection == "new-col\ttrue\nnext\t1") {
        "导出选区内容不符合预期: $exportedSelection"
    }
    println("Scenario OK")
}

private suspend fun settle(controller: SheetController) {
    while (controller.loading || controller.saving) {
        yield()
    }
}

private fun printCheckpoint(
    title: String,
    controller: SheetController,
) {
    val sheet = controller.activeSheet ?: error("当前没有激活工作表")
    println("-- $title --")
    println("sheet=${sheet.title}, rows=${sheet.rowCount}, columns=${sheet.columnCount}")
    println(
        listOf(
            "A1=${sheet.cell(SheetCellAddress(0, 0))?.raw.orEmpty()}",
            "B1=${sheet.cell(SheetCellAddress(0, 1))?.raw.orEmpty()}",
            "A2=${sheet.cell(SheetCellAddress(1, 0))?.raw.orEmpty()}",
            "B3=${sheet.cell(SheetCellAddress(2, 1))?.raw.orEmpty()}",
            "C2=${sheet.cell(SheetCellAddress(1, 2))?.raw.orEmpty()}",
        ).joinToString(" | "),
    )
}

@Composable
private fun SheetWorkbenchExampleApp() {
    val dataSource = remember { ExampleSheetDataSource(initialDocument = buildWorkbenchDocument()) }
    val controller = rememberSheetController(
        dataSource = dataSource,
        documentId = "example-sheet-ui",
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
            Header()
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
private fun Header() {
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
                text = "在线表格独立示例",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "默认运行桌面工作台，也支持单独跑 engine 场景与复制粘贴。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "命令: run / runEngineScenario",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private class ExampleSheetDataSource(
    initialDocument: SheetDocument,
) : SheetDataSource {
    private var document = initialDocument

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

private fun buildEngineDocument(): SheetDocument {
    return SheetDocument(
        documentId = "example-sheet-engine",
        activeSheetId = "engine-sheet",
        sheets = listOf(
            SheetPage(
                sheetId = "engine-sheet",
                title = "引擎场景",
                rowCount = 12,
                columnCount = 4,
                cells = mapOf(
                    SheetCellAddress(0, 0) to SheetCellValue.infer("region"),
                    SheetCellAddress(0, 1) to SheetCellValue.infer("status"),
                    SheetCellAddress(1, 0) to SheetCellValue.infer("华东"),
                    SheetCellAddress(1, 1) to SheetCellValue.infer("运行"),
                    SheetCellAddress(2, 0) to SheetCellValue.infer("华南"),
                    SheetCellAddress(2, 1) to SheetCellValue.infer("运行"),
                ),
            ),
        ),
    )
}

private fun buildWorkbenchDocument(): SheetDocument {
    return SheetDocument(
        documentId = "example-sheet-ui",
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
