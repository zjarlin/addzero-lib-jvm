# compose-native-component-sheet

Compose Multiplatform 在线表格 UI 壳。

- Maven 坐标：`site.addzero:compose-native-component-sheet`
- 本地路径：`lib/compose/compose-native-component-sheet`
- 依赖引擎：`site.addzero:compose-sheet-spi`

## 提供内容

- `SheetWorkbench`：工作簿级页面壳
- `SheetGrid`：单元格网格
- `SheetFormulaBar`：公式栏 / 原始值编辑条
- `SheetTabBar`：工作表切换页签
- `SheetSelectionActionBar`：范围填充、插删行列操作条

## 定位

这个模块只负责 UI，不负责数据存储、公式求值、协同同步和权限控制。
这些能力统一从 `compose-sheet-spi` 的 `SheetController` / `SheetDataSource` 接入。

## 当前交互

- 单击选中单元格，再次单击进入编辑
- 拖拽单元格可直接框选连续区域
- 选区支持复制、粘贴纯文本/TSV、向下填充、向右填充、插删行列

## 独立启动预览

- 运行桌面预览：
  `./gradlew :lib:compose:compose-native-component-sheet:previewSheetWorkbench`
