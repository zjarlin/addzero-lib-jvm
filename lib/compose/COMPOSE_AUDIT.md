# Compose 审计记录

本文件记录 `lib/compose` 这一轮注释补全与热点重构的处理结果。所有暂未落地的项都明确标记为 `defer`，避免后续误以为已经收口。

## 1. 失效依赖 / 失效模块名

- `done` `compose-native-component-hook` 已移除不存在的 `:lib:compose:compose-native-component` 依赖，改为真实使用的 `compose-native-component-autocomplet` 与 `compose-native-component-select`。
- `done` 可独立的 Compose 模块已重新加入 `settings.gradle.kts` 项目图，并补入 `tool-tree`、`compose-props` 本地 fallback、`lsi-core`、`lsi-ksp`。
- `done` `settings.gradle.kts` 已再次把 `compose-crud-spi`、`compose-eventbus` 从活动构建图移出，并删除重复 `include`，避免配置期被应用耦合模块拦截。
- `defer` `compose-crud-spi`、`compose-eventbus` 仍依赖 `:apps:kcloud:*` 和旧 `workbench-shell`，本轮不恢复构建，只保留审计记录。

## 2. 重复公开 API / 重复模块族

- `done` `compose-native-component-glass` 的旧 `SidebarItem` 侧栏 API 已标记废弃，并新增泛型 `GlassSidebar` / `CompactGlassSidebar` 入口。
- `done` `GlassShowcase` 示例页已迁到泛型侧栏调用方式，不再继续给新示例灌输 `SidebarItem` 包装写法。
- `defer` `glass-components`、`compose-native-component-glass`、`liquid-glass` 仍存在重复类族和相近视觉实现，本轮只先收口侧栏方向，未统一按钮/卡片/输入框实现。
- `defer` `GlassButton.kt`、`GlassCard.kt`、`GlassEffect.kt` 等跨模块文件名与包名重复，后续需要继续做模块级退役策略。

## 3. 类型不安全和包路径错位

- `done` `UseHook` 已统一回 `site.addzero.hook` 包，不再保留路径与包名错位。
- `done` `UseAutoComplate` 的 `null as T` 已移除，新增正确拼写的 `UseAutoComplete`，旧拼写改为废弃 typealias。
- `done` `UseHook` 不再通过 `this as T` 暴露自引用泛型状态，改为最小渲染/释放契约。
- `done` `AddTable`、`TableOriginal` 已移除不必要的 `inline/noinline/reified` 公开入口，避免 JVM 编译期的可见性和内联边界陷阱。
- `done` 表格默认行主键解析不再依赖异常的 `getIdExt` 包装，改为表格内部统一的 `id -> hashCode` 保底策略。
- `defer` `compose-native-component-table-pro` 的 JVM 主编译仍被 `compose-native-component-form` 的历史缺失依赖阻塞，属于上游模块问题，不是本轮表格源码回归。

## 4. 超大文件与职责混杂

- `done` `AdminWorkbenchScaffold.kt` 已抽出顶部工具条与工具按钮实现到 `AdminWorkbenchTopBar.kt`。
- `done` `AddTree.kt` 已抽出 `TreeScope` 与 `TreeScopeImpl` 到 `TreeScope.kt`，避免布局渲染和插槽协议继续堆在同一文件。
- `done` `PlaylistPlayerController.kt` 已抽出公开模型与 remember 入口到 `PlaylistPlayerModels.kt`。
- `done` `MediaPlaylistPlayer.kt` 已继续抽出头部卡片、队列项、封面与 URL 对话框到 `PlaylistPlayerPieces.kt`。
- `done` `AddTable.kt` 已抽出 `AddTableState`，把分页、排序、筛选、多选和高级搜索中的编辑态从入口函数里剥离。
- `done` `RenderAdvSearchDrawer.kt` 已降为纯输入组件，筛选条件的保存与清除重新收口回 `AddTableState`。
- `done` `TableOriginal.kt` 已继续拆成统一视觉样式、滚动内容区、表头、数据行、固定索引列和固定操作列，修复表头/表体列序不一致以及左右保留槽位错位问题。
- `defer` `MediaPlaylistPlayer.kt` 仍然偏大，剩余编排逻辑和歌词面板下一轮可继续拆。
- `defer` `AppSidebarScaffold.kt` 仍然承载状态、布局与拖拽细节，下一轮可继续拆出 layout/state 辅助文件。

## 5. 库模块直接耦合应用模块

- `defer` `compose-crud-spi`、`compose-eventbus` 直接依赖 `:apps:kcloud:*`，不符合可复用库边界。这一轮未改，因为会牵涉应用级解耦和 API 重新定义。

## 6. 机械暴露 `modifier` 或超长参数表

- `done` 旧玻璃侧栏 API 已从包装实体迁到泛型语义入口，减少调用方额外适配层。
- `done` 表格工具条、批量选择条和分页区已统一成紧凑的管理后台风格，默认按钮从图标优先改成可读文本动作按钮。
- `defer` `lib/compose` 里仍存在大量叶子组件机械暴露 `modifier`，本轮没有全量收敛，只优先处理了重复封装和失效边界问题。
- `defer` `AddTable` 仍保留长参数表以维持现有兼容调用，下一轮可以继续往 `config + slots` 结构收口。
- `defer` `media-playlist-player`、`app-sidebar`、部分 glass 组件仍有较长参数表，下一轮可以继续收束到 `Config` / `Slots` / `Decor` 风格。

## 验证备注

- `done` `:lib:compose:compose-native-component-glass:compileKotlinMetadata`
- `done` `:lib:compose:app-sidebar:compileKotlinMetadata`
- `done` `:lib:compose:compose-native-component-tree:compileKotlinMetadata`
- `done` `:lib:compose:compose-native-component-table-core:compileKotlinMetadata`
- `done` `:lib:compose:compose-native-component-table-core:compileKotlinJvm`
- `done` `:lib:compose:compose-native-component-table-core:jvmTest`
- `done` `:lib:compose:compose-native-component-table:compileKotlinMetadata`
- `done` `:lib:compose:compose-native-component-table:compileKotlinJvm`
- `done` `:lib:compose:compose-native-component-table:jvmTest`
- `done` `:lib:compose:compose-native-component-table-pro:compileKotlinMetadata`
- `defer` `:lib:compose:compose-native-component-table-pro:compileKotlinJvm`、`:lib:compose:compose-native-component-table-pro:jvmTest` 仍被 `compose-native-component-form` 的上游缺失符号阻塞，当前错误集中在 `RegexEnum`、`containsAnyIgnoreCase`、`getEnumValues` 等历史依赖缺失。
- `done` `:lib:compose:media-playlist-player:jvmTest`
- `done` `:lib:compose:compose-zh-fonts:jvmTest`
- `defer` `:lib:compose:glass-components:jvmTest` 仍因 `org.jetbrains.skiko.LibraryLoadException` 失败，当前更像本地图形运行时环境问题，不是本轮代码改动引入的新回归。
