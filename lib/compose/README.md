# Compose 相关模块

这一组目前体量不大，主要放 Compose UI 方向的组件试验。

## 组件索引与 Skill

- 总索引：`lib/compose/skills/index.md`
- 原生基础组件：`lib/compose/skills/native-basic.md`
- 表单组件：`lib/compose/skills/form.md`
- 表格组件：`lib/compose/skills/table.md`
- 树与命令树组件：`lib/compose/skills/tree.md`
- shadcn 风格组件：`lib/compose/skills/shadcn.md`
- 玻璃态与上传组件：`lib/compose/skills/glass-upload.md`
- 注解、Hook 与辅助状态：`lib/compose/skills/props-hook.md`

## 当前内容

- `compose-native-component-glass`：原生风格玻璃态组件方向的实验模块
- `compose-native-component-button`：按钮、删除、加载、图标按钮等基础操作入口
- `compose-native-component-form`：输入框、数字、日期、枚举、单选多选等表单组件
- `compose-native-component-table` / `table-pro`：原始表格、分页、排序、筛选、批量操作
- `compose-native-component-tree`：树、平铺树、命令树、树选择状态管理
- `shadcn-compose-component`：shadcn 风格主题与常见桌面组件

## 适合什么时候看

- 你在找 Compose UI 组件原型
- 你想确认仓库里有没有现成的桌面 / 多端视觉组件积累
- 你要给 AI 指定 `AddTable`、`Sidebar`、`AddTree`、`AddTextField` 这类现成组件
- 你想先按“场景”找组件，再决定依赖哪个模块

## 备注

- 这一组当前更偏实验和积累，不是 `lib/` 下最稳定的入口
- 如果是 AI 协作场景，优先先看 `lib/compose/skills/index.md`，再按场景进入对应索引
