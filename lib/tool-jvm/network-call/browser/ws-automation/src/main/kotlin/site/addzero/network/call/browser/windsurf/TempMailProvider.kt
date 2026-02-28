package site.addzero.network.call.browser.windsurf

import java.util.ServiceLoader

/**
 * 临时邮件 SPI 接口
 *
 * 通过 Java SPI 机制加载实现：
 * 1. 实现此接口
 * 2. 在 `META-INF/services/site.addzero.network.call.browser.windsurf.TempMailProvider` 中声明实现类全限定名
 * 3. [WindsurfRegistration] 会自动通过 [ServiceLoader] 加载
 *
 * 也可以手动传入实例，不依赖 SPI。
 */
interface TempMailProvider {

  /**
   * 创建一个临时邮箱并返回邮箱地址
   *
   * @return 临时邮箱地址，如 `"abc123@mail.tm"`
   */
  fun createEmail(): String

  /**
   * 获取 Windsurf 发送的 6 位验证码
   *
   * 实现应轮询邮箱收件箱，直到收到来自 Windsurf 的邮件并提取验证码。
   *
   * @param email 之前通过 [createEmail] 创建的邮箱地址
   * @param timeoutMs 最大等待时间（毫秒）
   * @return 6 位数字验证码
   * @throws IllegalStateException 超时未收到验证码
   */
  fun fetchVerificationCode(email: String, timeoutMs: Long = 60_000): String

  /**
   * 返回 [createEmail] 创建的邮箱登录密码，供账号信息存储使用
   *
   * 默认返回空字符串，实现类应覆盖此方法以返回真实密码。
   */
  fun getMailPassword(): String = ""

  /**
   * 登录已有的临时邮箱（用于复用之前创建但未注册成功的邮箱）
   *
   * 登录后 [fetchVerificationCode] 应能正常收取该邮箱的验证码。
   *
   * @param email    邮箱地址
   * @param password 邮箱密码
   */
  fun loginExisting(email: String, password: String) {
    throw UnsupportedOperationException("loginExisting not implemented by ${javaClass.name}")
  }

  companion object {
    /**
     * 通过 Java SPI 加载第一个可用的 [TempMailProvider] 实现
     *
     * @return 实现实例，未找到时返回 null
     */
    fun loadFromSpi(): TempMailProvider {
      val firstOrNull = ServiceLoader.load(TempMailProvider::class.java).firstOrNull()
      return firstOrNull?:  TempMailProviderImpl()
    }
  }
}
