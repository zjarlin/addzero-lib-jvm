package site.addzero.network.call.browser.core

/**
 * 表单字段动作类型
 */
enum class FieldType {
  /** 输入框填值 */
  INPUT,
  /** 点击按钮/链接 */
  CLICK,
  /** 勾选 checkbox */
  CHECK,
}

/**
 * 声明式表单字段定义
 *
 * @param name      字段名（用于日志和调试产物命名，如 "email"、"submit"）
 * @param selectors selector 列表，按优先级依次尝试。
 *                  支持以下格式：
 *                  - CSS selector：如 `"input[name='email']"`
 *                  - `"label:xxx"` → `page.getByLabel("xxx")`
 *                  - `"placeholder:xxx"` → `page.getByPlaceholder("xxx")`
 *                  - `"role:button:Continue"` → `page.getByRole(BUTTON, name="Continue")`
 * @param value     要填入的值（[FieldType.CLICK]/[FieldType.CHECK] 时可为空串）
 * @param required  true → 找不到时抛异常并输出调试产物；false → 静默跳过
 * @param type      字段类型，默认 [FieldType.INPUT]
 */
data class FormFieldDef(
  val name: String,
  val selectors: List<String>,
  val value: String = "",
  val required: Boolean = false,
  val type: FieldType = FieldType.INPUT,
)
