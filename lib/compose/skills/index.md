# Compose 组件 Skill 索引

这个目录不是再讲一遍源码，而是给人和 AI 一个统一入口：先按场景选组件，再去看对应模块和 skill。

## 怎么用这个索引

1. 先看你要解决的是“基础 UI / 表单 / 表格 / 树 / shadcn / 玻璃态上传 / 注解辅助”哪一类。
2. 打开对应索引文件，确认推荐模块、入口组件、最小依赖和示例写法。
3. 如果是 AI 协作，再配合对应 skill 名称和触发关键词，让模型优先复用现成组件。

## 场景总览

| 场景 | 仓库索引文件 | 推荐 skill | 常见触发关键词 |
| --- | --- | --- | --- |
| 原生基础组件、布局、搜索、选择、文本、提示 | `lib/compose/skills/native-basic.md` | `addzero-compose-native-basic` | `AddButton`、`AddCard`、`AddSearchBar`、`AddSelect`、`AddTabs`、`AddDrawer`、`BeautifulText` |
| 表单输入、枚举选择、日期时间、数字、动态表单 | `lib/compose/skills/form.md` | `addzero-compose-form` | `AddTextField`、`AddPasswordField`、`AddEnumSelector`、`AddDateField`、`AddMoneyField`、`DynamicFormItem` |
| 表格、分页、排序、筛选、批量操作 | `lib/compose/skills/table.md` | `addzero-compose-table` | `AddTable`、`TableOriginal`、`ColumnConfig`、`StatePagination`、`RenderFilterButton`、`分页表格` |
| 树、平铺树、命令树、树选择状态 | `lib/compose/skills/tree.md` | `addzero-compose-tree` | `AddTree`、`AddFlatTree`、`AddTreeWithCommand`、`TreeViewModel`、`TreeSelectionManager`、`树形导航` |
| shadcn 风格主题与通用组件 | `lib/compose/skills/shadcn.md` | `addzero-compose-shadcn` | `ShadcnTheme`、`Sidebar`、`Dialog`、`Drawer`、`Select`、`Tabs`、`Sonner` |
| 玻璃态视觉、文件选择、上传队列 | `lib/compose/skills/glass-upload.md` | `addzero-compose-glass-upload` | `GlassButton`、`GlassCard`、`GlassSidebar`、`AddFileUploader`、`AddMultiFilePicker`、`UploadManagerUI` |
| 注解生成、Hook 封装、自动状态辅助 | `lib/compose/skills/props-hook.md` | `addzero-compose-props-hook` | `ComposeAssist`、`AssistExclude`、`UseSelect`、`UseAutoComplate`、`AutoCompleteState` |

## 模块分组

### 业务 UI 组件

- `compose-native-component-button`
- `compose-native-component-card`
- `compose-native-component-form`
- `compose-native-component-searchbar`
- `compose-native-component-select`
- `compose-native-component-table`
- `compose-native-component-table-pro`
- `compose-native-component-tree`
- `compose-native-component-high-level`
- `compose-klibs-component`
- `shadcn-compose-component`
- `compose-native-component-glass`

### 支撑模块

- `compose-native-component`：文本、toast、自动补全、通用 `Modifier` 扩展
- `compose-native-component-assist`：自动聚焦、标题图标、统一取 id 工具
- `compose-native-component-hook`：`UseSelect` / `UseAutoComplate` 这类状态封装
- `compose-model-component`：表格与低代码组件相关模型
- `compose-native-component-table-core`：表格排序、筛选、分页与布局配置模型
- `compose-props-annotations` / `compose-props-processor`：`@ComposeAssist` 代码生成

## 选型建议

- 要快搭桌面工作台壳层：先看 `native-basic.md` 和 `shadcn.md`
- 要做 CRUD：先看 `form.md` + `table.md`
- 要做资源、目录、菜单或命令面板：先看 `tree.md`
- 要做展示型视觉稿：先看 `glass-upload.md`
- 要减少 Compose 参数样板和状态胶水：先看 `props-hook.md`
