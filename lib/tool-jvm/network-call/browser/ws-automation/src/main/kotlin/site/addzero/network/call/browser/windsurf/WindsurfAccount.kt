package site.addzero.network.call.browser.windsurf

/**
 * 临时邮箱 + Windsurf 账号信息
 *
 * 说明：邮箱创建后就会落盘，注册成功/失败会更新状态字段。
 *
 * @param windsurfEmail    Windsurf 登录邮箱（临时邮箱地址）
 * @param windsurfPassword Windsurf 登录密码
 * @param mailPassword     临时邮箱的登录密码（用于后续收件箱查看）
 * @param status           注册状态（邮箱已创建 / 已注册 / 失败）
 * @param errorMessage     失败原因（可选）
 * @param registeredAt     记录写入时间（ISO-8601）
 */
data class WindsurfAccount(
  val windsurfEmail: String,
  val windsurfPassword: String,
  val mailPassword: String,
  val status: WindsurfAccountStatus = WindsurfAccountStatus.EMAIL_CREATED,
  val errorMessage: String? = null,
  val registeredAt: String = java.time.Instant.now().toString(),
)

/**
 * Windsurf 注册状态
 */
enum class WindsurfAccountStatus {
  /** 已创建临时邮箱，但注册尚未完成 */
  EMAIL_CREATED,
  /** 注册成功 */
  REGISTERED,
  /** 注册失败 */
  FAILED,
}
