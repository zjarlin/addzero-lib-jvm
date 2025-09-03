# TableOriginal Context 优化总结

## 🎯 优化目标

解决使用大的context会导致不必要重组的问题，通过合理拆分ViewModel来渲染不同的子组件。

## 🔧 优化策略

### 问题分析
使用`context(vm: TableViewModel<*, *>)`会导致：
1. **过度重组** - 任何ViewModel属性变化都会触发所有子组件重组
2. **性能问题** - 不相关的状态变化也会导致重新渲染
3. **依赖混乱** - 子组件依赖了不需要的状态

### 解决方案
将大的context拆分为更细粒度的参数传递，每个子组件只接收它真正需要的数据。

## 🏗️ 重构后的架构

### 1. RenderFixedIndexColumn - 序号列组件
**只需要的数据：**
- `verticalScrollState: LazyListState?` - 滚动状态
- `data: List<T>` - 数据列表（用于计算数量）
- `config: TableConfig` - 表头配置

```kotlin
@Composable
fun <T> RenderFixedIndexColumn(
    verticalScrollState: LazyListState?,
    data: List<T>,
    config: TableConfig,
    modifier: Modifier = Modifier
)
```

**优化效果：**
- ✅ 只在滚动状态、数据数量或表头配置变化时重组
- ✅ 不受其他无关状态影响

### 2. RenderFixedActionColumn - 操作列组件
**只需要的数据：**
- `verticalScrollState: LazyListState?` - 滚动状态
- `data: List<T>` - 数据列表
- `config: TableConfig` - 表头配置
- `slots: TableSlots<T>` - 操作插槽

```kotlin
@Composable
fun <T> RenderFixedActionColumn(
    verticalScrollState: LazyListState?,
    data: List<T>,
    config: TableConfig,
    slots: TableSlots<T>,
    modifier: Modifier = Modifier
)
```

**优化效果：**
- ✅ 只在滚动状态、数据或操作插槽变化时重组
- ✅ 不受列配置变化影响

### 3. RenderTableHeaderRow - 表头行组件
**只需要的数据：**
- `columns: List<C>` - 列配置
- `getColumnKey: (C) -> String` - 列键获取函数
- `getColumnLabel: @Composable (C) -> Unit` - 列标签渲染函数
- `config: TableConfig` - 表头配置
- `horizontalScrollState: ScrollState?` - 水平滚动状态

```kotlin
@Composable
fun <C> RenderTableHeaderRow(
    columns: List<C>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    config: TableConfig,
    horizontalScrollState: ScrollState?
)
```

**优化效果：**
- ✅ 只在列配置或表头配置变化时重组
- ✅ 不受数据变化影响

### 4. RenderCompleteDataRow - 数据行组件
**只需要的数据：**
- `item: T` - 行数据
- `index: Int` - 行索引
- `columns: List<C>` - 列配置
- `getColumnKey: (C) -> String` - 列键获取函数
- `getCellContent: @Composable (item: T, column: C) -> Unit` - 单元格内容渲染函数
- `slots: TableSlots<T>` - 行插槽
- `horizontalScrollState: ScrollState?` - 水平滚动状态

```kotlin
@Composable
fun <T, C> RenderCompleteDataRow(
    item: T,
    index: Int,
    columns: List<C>,
    getColumnKey: (C) -> String,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    slots: TableSlots<T>,
    horizontalScrollState: ScrollState?
)
```

**优化效果：**
- ✅ 只在行数据、列配置或插槽变化时重组
- ✅ 不受其他行数据变化影响

## 📊 性能优化对比

### 优化前（使用大context）
```kotlin
// ❌ 问题：所有组件都依赖整个ViewModel
context(vm: TableViewModel<*, *>)
@Composable
fun RenderFixedIndexColumn() {
    // 任何ViewModel属性变化都会重组
}
```

**问题：**
- 🔴 过度重组 - 数据变化时序号列也重组
- 🔴 性能浪费 - 不相关的状态变化也触发重组
- 🔴 依赖混乱 - 组件依赖了不需要的状态

### 优化后（细粒度参数）
```kotlin
// ✅ 解决：只传递真正需要的参数
@Composable
fun <T> RenderFixedIndexColumn(
    verticalScrollState: LazyListState?,
    data: List<T>,
    config: TableConfig,
    modifier: Modifier = Modifier
) {
    // 只在相关参数变化时重组
}
```

**优势：**
- ✅ 精确重组 - 只在相关状态变化时重组
- ✅ 性能优化 - 减少不必要的重组
- ✅ 依赖清晰 - 明确每个组件的依赖关系

## 🎯 重组优化效果

### 场景1：数据更新
- **优化前**: 数据变化 → 所有组件重组
- **优化后**: 数据变化 → 只有数据相关组件重组

### 场景2：滚动操作
- **优化前**: 滚动状态变化 → 所有组件重组
- **优化后**: 滚动状态变化 → 只有滚动相关组件重组

### 场景3：配置更新
- **优化前**: 配置变化 → 所有组件重组
- **优化后**: 配置变化 → 只有配置相关组件重组

## 🔄 调用方式

### 在主组件中的使用
```kotlin
@Composable
fun <T, C> RenderTableMainContent(
    vm: TableViewModel<T, C>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // 主内容滚动区域
        RenderTableScrollableContent(vm)

        // 序号列 - 只传递需要的参数
        with(vm) {
            RenderFixedIndexColumn(
                verticalScrollState = verticalScrollState,
                data = data,
                config = config,
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            )
        }

        // 操作列 - 只传递需要的参数
        with(vm) {
            RenderFixedActionColumn(
                verticalScrollState = verticalScrollState,
                data = data,
                config = config,
                slots = slots,
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(1f)
            )
        }
    }
}
```

## 📈 性能提升

### 重组次数减少
- **序号列**: 只在滚动、数据数量、表头配置变化时重组
- **操作列**: 只在滚动、数据、操作插槽变化时重组
- **表头行**: 只在列配置、表头配置变化时重组
- **数据行**: 只在行数据、列配置、插槽变化时重组

### 内存使用优化
- 减少了不必要的状态订阅
- 降低了组件间的耦合度
- 提高了组件的可复用性

## 🎉 总结

通过将大的context拆分为细粒度的参数传递，我们成功地：

1. **✅ 解决了过度重组问题** - 每个组件只在相关状态变化时重组
2. **✅ 提升了性能** - 减少了不必要的重新渲染
3. **✅ 明确了依赖关系** - 每个组件的依赖关系更加清晰
4. **✅ 提高了可维护性** - 组件职责更加单一
5. **✅ 增强了可测试性** - 组件更容易进行单元测试

这种优化策略在大型表格组件中特别有效，能够显著提升用户体验和应用性能！
