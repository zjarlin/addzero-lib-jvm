package site.addzero.network.call.emailcode.tempmail

import site.addzero.network.call.emailcode.model.EmailMailboxCreateRequest
import site.addzero.network.call.emailcode.model.EmailMailboxLoginRequest
import site.addzero.network.call.emailcode.spi.EmailCodeMailbox
import site.addzero.network.call.emailcode.spi.EmailCodeProvider

class TempMailEmailCodeProvider(
    private val client: TempMailClient = TempMailClient(),
) : EmailCodeProvider {
    override val id: String = ID

    override fun createMailbox(request: EmailMailboxCreateRequest): EmailCodeMailbox =
        TempMailEmailCodeMailbox(
            client = client,
            mailbox = client.createMailboxAndLogin(
                TempMailCreateMailboxRequest(
                    prefix = request.prefix,
                    passwordLength = request.passwordLength,
                    preferredDomain = request.preferredDomain,
                ),
            ),
        )

    override fun login(request: EmailMailboxLoginRequest): EmailCodeMailbox =
        TempMailEmailCodeMailbox(
            client = client,
            mailbox = TempMailMailbox(
                address = request.address,
                password = request.credential,
                accountId = "",
                token = client.createToken(request.address, request.credential),
            ),
        )

    companion object {
        const val ID = "mail.tm"
    }
}
