package site.addzero.network.call.browser.windsurf

import site.addzero.network.call.tempmail.TempMailClient
import site.addzero.network.call.tempmail.TempMailMailbox

/**
 * 基于 mail.tm 的 [TempMailProvider] 实现
 *
 * 创建临时邮箱后内部保存 token，轮询收件箱找到 Windsurf 验证码邮件，
 * 从 subject 中提取 6 位数字验证码。
 */
class TempMailProviderImpl : TempMailProvider {

  private val client = TempMailClient()

  private var mailbox: TempMailMailbox? = null

  override fun createEmail(): String {
    val mb = client.createMailboxAndLogin(prefix = "ws")
    mailbox = mb
    return mb.address
  }

  override fun getMailPassword(): String = mailbox?.password ?: ""

  override fun loginExisting(email: String, password: String) {
    val token = client.createToken(email, password)
    mailbox = TempMailMailbox(
      address = email,
      password = password,
      accountId = "",
      token = token,
    )
    println("[TempMailProviderImpl] logged in to existing mailbox: $email")
  }

  override fun fetchVerificationCode(email: String, timeoutMs: Long): String {
    val token = mailbox?.token
      ?: error("createEmail() or loginExisting() must be called before fetchVerificationCode()")

    // 先快照当前已有的邮件 ID，后续只从新邮件中提取验证码（避免拿到过期验证码）
    val existingIds = runCatching { client.listMessages(token) }.getOrElse { emptyList() }
      .map { it.id }.toSet()
    println("[TempMailProviderImpl] snapshot: ${existingIds.size} existing messages, will only check new ones")

    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
      val messages = runCatching { client.listMessages(token) }.getOrElse { emptyList() }
        .filter { it.id !in existingIds } // 只看新到达的邮件
        .sortedByDescending { it.createdAt }

      for (msg in messages) {
        val code = extractCode(msg.subject)
          ?: runCatching { client.getMessage(token, msg.id) }.getOrNull()?.let { detail ->
            extractCode(detail.text)
              ?: extractCode(detail.html)
              ?: extractCode(detail.subject)
          }
        if (code != null) {
          println("[TempMailProviderImpl] found code=$code from: subject='${msg.subject}', from=${msg.fromAddress}, createdAt=${msg.createdAt}")
          return code
        }
      }

      println("[TempMailProviderImpl] no new code yet (${messages.size} new messages), retrying in 3s... (${(deadline - System.currentTimeMillis()) / 1000}s left)")
      Thread.sleep(3_000)
    }

    error("Verification code not received within ${timeoutMs / 1000}s for $email")
  }

  private fun extractCode(text: String?): String? {
    if (text.isNullOrBlank()) return null
    // 先尝试连续 6 位数字
    Regex("\\b(\\d{6})\\b").find(text)?.groupValues?.get(1)?.let { return it }
    // 兼容 1 2 3 4 5 6 或 12 34 56 形式
    val spaced = Regex("(?:\\d\\s*){6}").find(text)?.value?.filter { it.isDigit() }
    return spaced?.takeIf { it.length == 6 }
  }
}
