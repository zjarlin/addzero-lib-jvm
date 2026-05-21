package site.addzero.network.call.browser.windsurf

import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailMailboxCreateRequest
import site.addzero.network.call.emailcode.model.EmailMailboxLoginRequest
import site.addzero.network.call.emailcode.spi.EmailCodeMailbox
import site.addzero.network.call.emailcode.spi.EmailCodeProviders
import site.addzero.network.call.emailcode.tempmail.TempMailEmailCodeProvider

/**
 * 基于统一邮箱接码 SPI 的 mail.tm 适配实现
 *
 * 创建临时邮箱后内部保存邮箱会话，轮询收件箱找到 Windsurf 验证码邮件，
 * 再由统一提取器解析 6 位数字验证码。
 */
class TempMailProviderImpl : TempMailProvider {
  private val provider = EmailCodeProviders.firstOrNull(TempMailEmailCodeProvider.ID)
    ?: TempMailEmailCodeProvider()

  private var mailbox: EmailCodeMailbox? = null

  override fun createEmail(): String {
    val createdMailbox = provider.createMailbox(
      EmailMailboxCreateRequest(prefix = "qwq"),
    )
    mailbox = createdMailbox
    return createdMailbox.address
  }

  override fun getMailPassword(): String = mailbox?.loginSecret.orEmpty()

  override fun loginExisting(email: String, password: String) {
    mailbox = provider.login(
      EmailMailboxLoginRequest(
        address = email,
        credential = password,
      ),
    )
    println("[TempMailProviderImpl] logged in to existing mailbox: $email")
  }

  override fun fetchVerificationCode(email: String, timeoutMs: Long): String {
    val currentMailbox = mailbox
      ?: error("createEmail() or loginExisting() must be called before fetchVerificationCode()")

    val code = currentMailbox.awaitCode(
      EmailCodeRequest(timeoutMs = timeoutMs),
    )
    println("[TempMailProviderImpl] found code=${code.code} from: subject='${code.subject}', from=${code.fromAddress}")
    return code.code
  }
}
