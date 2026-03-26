# shadcn 风格组件索引

## 覆盖模块

- `shadcn-compose-component`

## 适合场景

- 需要统一 `ShadcnTheme`
- 需要桌面工作台常见原子组件和壳层组件
- 想要 `Sidebar`、`Dialog`、`Drawer`、`Popover`、`Tabs`、`Select` 这类成体系组件

## 依赖入口

```kotlin
implementation(projects.lib.compose.shadcnComposeComponent)
```

## 先包主题

```kotlin
ShadcnTheme {
    AppContent()
}
```

## 组件速查

### 主题与样式

- `ShadcnTheme`
- `LightColors` / `DarkColors`
- `Radius`
- `Shadows`

### 常用基础组件

- `Button`
- `Input`
- `Checkbox`
- `Switch`
- `RadioGroup`
- `Badge`
- `Progress`
- `Slider`
- `Avatar`
- `Skeleton`

### 浮层与反馈

- `Dialog`
- `AlertDialog`
- `Drawer`
- `Popover`
- `DropdownMenu`
- `Alert`
- `Sonner`
- `SonnerHost`
- `SonnerProvider`

### 结构组件

- `Card`
- `Tabs`
- `Accordion`
- `Calendar`
- `DatePicker`
- `Select`
- `ComboBox`
- `Carousel`

### 侧边栏体系

- `SidebarProvider`
- `Sidebar`
- `SidebarTrigger`
- `SidebarInset`
- `SidebarLayout`
- `SidebarContent`
- `SidebarHeader`
- `SidebarFooter`
- `SidebarMenu`
- `SidebarMenuItem`
- `SidebarMenuButton`

## 最小组合示例

```kotlin
@Composable
fun SettingsPage() {
    ShadcnTheme {
        SidebarProvider {
            SidebarLayout(
                sidebarHeader = { Text("系统设置") },
                sidebarContent = {
                    SidebarMenu {
                        SidebarMenuItem {
                            SidebarMenuButton(onClick = {}) { Text("通用") }
                        }
                    }
                }
            ) {
                Card {
                    CardHeader { CardTitle("设置") }
                    CardContent {
                        Input(value = "", onValueChange = {})
                    }
                }
            }
        }
    }
}
```

## 使用原则

- 如果页面大部分组件都来自这里，最外层必须先包 `ShadcnTheme`
- `SidebarLayout` 适合一体式壳层；`SidebarInset` 适合你自己控制外层布局
- 需要 toast / transient message 时优先走 `Sonner`
