# Compose 相关模块

这一组目前体量不大，主要放 Compose UI 方向的组件试验。

## 组件索引与 Skill

- 统一入口已迁移到 `cmp-convention`：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-lib-compose.md`
- 原生基础组件：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-native-basic.md`
- 表单组件：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-form.md`
- 表格组件：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-table.md`
- 树与命令树组件：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-tree.md`
- shadcn 风格组件：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-shadcn.md`
- 玻璃态与上传组件：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-glass-upload.md`
- 注解、Hook 与辅助状态：
  `/Users/zjarlin/.agents/skills/cmp-convention/references/addzero-compose-props-hook.md`

## 当前内容

- `compose-native-component-glass`：原生风格玻璃态组件方向的实验模块
- `app-sidebar`：当前推荐的通用侧栏与工作台骨架入口
- `compose-workbench-design`：工作台级按钮等设计原语
- `compose-workbench-shell`：工作台场景切换、树侧栏、内容容器与顶栏动作接口
- `compose-native-component-button`：按钮、删除、加载、图标按钮等基础操作入口
- `compose-native-component-form`：输入框、数字、日期、枚举、单选多选等表单组件
- `compose-native-component-autocomplet`：旧模块名保留，承载自动补全输入组件与迁入的 `site.addzero.autocomplete` API
- `compose-native-component-knowledgegraph`：知识图谱、节点筛选、力导向布局、JVM 抽屉代码查看
- `compose-native-component-table` / `table-pro`：原始表格、分页、排序、筛选、批量操作
- `compose-native-component-tree`：树、平铺树、命令树、树选择状态管理
- `shadcn-compose-component`：shadcn 风格主题与常见桌面组件
- `glass-components` / `liquid-glass`：当前主承载的玻璃态组件方向，优先复用这里的泛型语义 API

## 适合什么时候看

- 你在找 Compose UI 组件原型
- 你想确认仓库里有没有现成的桌面 / 多端视觉组件积累
- 你要给 AI 指定 `AddTable`、`Sidebar`、`AddTree`、`AddTextField` 这类现成组件
- 你想先按“场景”找组件，再决定依赖哪个模块

## 备注

- 这一组当前更偏实验和积累，不是 `lib/` 下最稳定的入口
- 侧栏方向优先使用 `app-sidebar`，旧式 `SidebarItem` 包装 API 进入兼容迁移阶段
- 玻璃态方向优先看 `glass-components` 和 `liquid-glass`，`compose-native-component-glass` 主要保留兼容入口
- 如果是 AI 协作场景，优先先看 `cmp-convention` 里的 `addzero-lib-compose.md`，再按场景进入对应参考文档
