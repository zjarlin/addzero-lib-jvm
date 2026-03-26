# 表单组件索引

## 覆盖模块

- `compose-native-component-form`
- `compose-native-component-button`
- `compose-native-component-tree`（表单里的选择器依赖）

## 适合场景

- 登录、注册、账号设置、设备配置表单
- 金额、整数、小数、百分比输入
- 日期、时间、日期时间选择
- 单选、多选、枚举选择、状态切换
- 根据字段元数据动态渲染表单项

## 依赖入口

```kotlin
implementation(projects.lib.compose.composeNativeComponentForm)
```

## 组件速查

### 文本与账号类

- `AddTextField`
- `AddPasswordField`
- `AddUsernameField`
- `AddPhoneField`
- `AddEmailField`
- `AddUrlField`
- `AddBankCardField`
- `AddIdCardField`
- `AddIconText`

### 数字类

- `AddNumberField`
- `AddIntegerField`
- `AddDecimalField`
- `AddMoneyField`
- `AddPercentageField`

### 日期类

- `AddDateField`
- `AddTimeField`
- `AddDateTimeField`

### 选择类

- `AddEnumSelector`
- `AddGenericSingleSelector`
- `AddGenericMultiSelector`
- `AddSelectedChips`
- `AddStatusSelectorRow`
- `AddSwitchField`

### 动态表单

- `DynamicFormItem`
- `RemoteValidationConfig`

## 最小组合示例

```kotlin
@Composable
fun AccountForm(
    username: String,
    password: String,
    enabled: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
) {
    Column {
        AddUsernameField(value = username, onValueChange = onUsernameChange)
        AddPasswordField(value = password, onValueChange = onPasswordChange)
        AddSwitchField(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            label = "启用账号"
        )
    }
}
```

```kotlin
@Composable
fun FilterForm(
    amount: String,
    date: String,
    status: String,
    onAmountChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
) {
    Column {
        AddMoneyField(value = amount, onValueChange = onAmountChange)
        AddDateField(value = date, onValueChange = onDateChange)
        AddGenericSingleSelector(
            options = listOf("启用", "停用"),
            selectedOption = status,
            onOptionSelected = onStatusChange
        )
    }
}
```

## 使用原则

- 有现成专用输入框时，不要退回裸 `TextField`
- 枚举和状态值优先走 `AddEnumSelector` / `AddStatusSelectorRow`
- 多字段元数据驱动表单，优先试 `DynamicFormItem`
- 表单提交、保存、搜索按钮可以直接复用 `compose-native-component-button`
