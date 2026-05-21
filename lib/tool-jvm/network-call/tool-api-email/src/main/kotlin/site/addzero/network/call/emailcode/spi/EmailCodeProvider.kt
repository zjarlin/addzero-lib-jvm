package site.addzero.network.call.emailcode.spi

import site.addzero.network.call.emailcode.model.EmailMailboxCreateRequest
import site.addzero.network.call.emailcode.model.EmailMailboxLoginRequest

interface EmailCodeProvider {
    val id: String

    fun createMailbox(request: EmailMailboxCreateRequest = EmailMailboxCreateRequest()): EmailCodeMailbox {
        throw UnsupportedOperationException("Provider $id does not support mailbox creation")
    }

    fun login(request: EmailMailboxLoginRequest): EmailCodeMailbox
}
