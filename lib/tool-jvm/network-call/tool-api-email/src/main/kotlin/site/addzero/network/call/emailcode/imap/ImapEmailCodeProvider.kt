package site.addzero.network.call.emailcode.imap

import site.addzero.network.call.emailcode.model.EmailMailboxLoginRequest
import site.addzero.network.call.emailcode.spi.EmailCodeMailbox
import site.addzero.network.call.emailcode.spi.EmailCodeProvider

open class ImapEmailCodeProvider(
    override val id: String,
    private val serverConfig: ImapServerConfig,
) : EmailCodeProvider {
    override fun login(request: EmailMailboxLoginRequest): EmailCodeMailbox =
        ImapEmailCodeMailbox(
            providerId = id,
            serverConfig = serverConfig,
            loginRequest = request,
        )
}
