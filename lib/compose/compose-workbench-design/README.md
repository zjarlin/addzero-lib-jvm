# compose-workbench-design

KCloud 当前沉淀出来的 workbench 设计原语，主要承接统一按钮族。

- Maven coordinate: `site.addzero:compose-workbench-design`
- Local module path: `lib/compose/compose-workbench-design`

## Scope

- `WorkbenchButton`
- `WorkbenchPillButton`
- `WorkbenchOutlinedButton`
- `WorkbenchFilledTonalButton`
- `WorkbenchTextButton`
- `WorkbenchIconButton`
- `WorkbenchActionButton`

## Usage

```kotlin
WorkbenchActionButton(
    text = "保存",
    imageVector = Icons.Default.Save,
    onClick = ::save,
)
```

## Notes

- 所有按钮外观统一基于 shadcn Compose `Button`
- 不保留 app 专属兼容包名
