# tool-api-browser-automation

Kotlin 浏览器自动化填表工具（基于 Playwright），**通用引擎 + 业务示例**。

## 模块结构

```
src/main/kotlin/site/addzero/network/call/browser/
├── core/                              ← 通用层（任意站点可复用）
│   ├── BrowserAutomationOptions.kt    #   浏览器启动配置
│   ├── FormFieldDef.kt                #   声明式字段定义
│   └── BrowserFormFiller.kt           #   核心引擎
└── windsurf/                          ← 业务层（Windsurf 注册示例）
    ├── WindsurfRegisterForm.kt        #   业务表单 + 选项
    └── WindsurfRegisterAutomation.kt  #   组装字段 + 调用引擎
```

## 依赖

- `com.microsoft.playwright:playwright:1.45.0`
- 优先使用系统已安装的 Chrome（macOS/Linux/Windows 自动检测）
- 如果系统没有 Chrome，会 fallback 到 Playwright 内置浏览器

## 快速开始

### 1) 通用方式 — 直接用 `BrowserFormFiller`

```kotlin
import site.addzero.network.call.browser.core.*

BrowserFormFiller.fill(
  url = "https://example.com/register",
  fields = listOf(
    FormFieldDef("username", listOf("input[name='username']"), "john_doe", required = true),
    FormFieldDef("email", listOf("input[type='email']"), "john@example.com", required = true),
    FormFieldDef("password", listOf("input[type='password']"), "Secret123!", required = true),
  ),
  options = BrowserAutomationOptions(headless = false),
  submitSelectors = listOf("button[type='submit']"),
)
```

### 2) 业务方式 — Windsurf 注册示例

```kotlin
import site.addzero.network.call.browser.windsurf.*
import site.addzero.network.call.browser.core.BrowserAutomationOptions

WindsurfRegisterAutomation().openAndFill(
  form = WindsurfRegisterForm(
    email = "demo@example.com",
    firstName = "Demo",
    lastName = "User",
  ),
  options = WindsurfRegisterOptions(
    autoSubmit = false,
    automation = BrowserAutomationOptions(headless = false),
  ),
)
```

## 核心概念

### FormFieldDef — 声明式字段定义

| 属性 | 说明 |
|------|------|
| `name` | 字段名，用于日志和调试产物命名 |
| `selectors` | CSS selector 列表，按优先级依次尝试 |
| `value` | 要填的值（CLICK 类型可为空） |
| `required` | `true` → 找不到时抛异常并输出调试产物 |
| `type` | `INPUT`（填值）或 `CLICK`（点击） |

### BrowserAutomationOptions — 启动配置

| 属性 | 默认值 | 说明 |
|------|--------|------|
| `headless` | `true` | 是否无头模式 |
| `timeoutMs` | `30000` | 全局超时（毫秒） |
| `slowMoMs` | `0` | 每步操作间隔（毫秒），方便观察 |
| `debug` | `false` | 开启后，找不到字段时输出截图/HTML/inputs.json |
| `artifactsDir` | 系统临时目录 | 调试产物保存路径 |

## 调试指南 — 遇到新表单怎么办

### 第 1 步：开启 debug 模式跑一次

```kotlin
BrowserFormFiller.fill(
  url = "https://some-site.com/form",
  fields = listOf(
    FormFieldDef("email", listOf("input[type='email']"), "test@test.com", required = true),
  ),
  options = BrowserAutomationOptions(
    headless = false,
    debug = true,
    artifactsDir = "/tmp/my-debug",
  ),
)
```

### 第 2 步：查看调试产物

运行失败后，在 `artifactsDir` 目录下会生成：

| 文件 | 作用 |
|------|------|
| `*-<fieldName>.png` | 页面截图，看实际页面长什么样 |
| `*-<fieldName>.html` | 完整 HTML，可搜索 `input`/`button` 标签 |
| `*-<fieldName>-inputs.json` | **关键文件** — 所有表单元素的属性 |

### 第 3 步：看 inputs.json 写 selectors

`inputs.json` 示例：

```json
[
  {
    "tag": "input",
    "type": "email",
    "name": "user_email",
    "id": "email-field",
    "placeholder": "Enter your email",
    "autocomplete": "email",
    "ariaLabel": null,
    "dataTestId": "register-email",
    "className": "form-input",
    "visible": true
  }
]
```

根据这些属性写 selectors：

```kotlin
val emailSelectors = listOf(
  "#email-field",                      // 优先用 id
  "input[name='user_email']",          // 其次 name
  "input[data-testid='register-email']", // 或 data-testid
  "input[type='email']",               // 最后按 type 兜底
)
```

### 第 4 步：组装 FormFieldDef 列表

```kotlin
val fields = listOf(
  FormFieldDef("email", emailSelectors, "test@test.com", required = true),
  // ... 其他字段
)
BrowserFormFiller.fill(url, fields, options)
```

### 第 5 步：再跑一次验证

关闭 debug 模式正常运行即可。

## 填写策略

`BrowserFormFiller` 对每个 input 字段使用三级 fallback 策略：

1. **click + fill** — 先点击激活元素再填值（大多数现代表单需要这个）
2. **clear + fill** — 清空后填值
3. **clear + type** — 清空后模拟键盘逐字输入（最兼容，应对一些拦截 fill 事件的框架）

此外还支持：
- **跨 iframe 查找** — 自动在所有 frame 中尝试
- **等待可见** — 对每个 selector 等待 2s 可见，避免 DOM 存在但未渲染的情况

## 说明

- 默认 `autoSubmit = false`，只填表不提交
- 导航使用 `waitUntil=DOMCONTENTLOADED`，不等 `load`（避免超时）
- 系统 Chrome 路径跨平台自动检测（macOS/Linux/Windows）
