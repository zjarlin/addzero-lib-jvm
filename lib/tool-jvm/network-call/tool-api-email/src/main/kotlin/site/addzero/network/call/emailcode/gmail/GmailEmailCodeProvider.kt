package site.addzero.network.call.emailcode.gmail

import site.addzero.network.call.emailcode.imap.ImapEmailCodeProvider
import site.addzero.network.call.emailcode.imap.ImapMailboxPresets

class GmailEmailCodeProvider : ImapEmailCodeProvider(
    id = ID,
    serverConfig = ImapMailboxPresets.GMAIL,
) {
    companion object {
        const val ID = "gmail"
    }
}
