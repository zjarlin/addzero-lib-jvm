package site.addzero.network.call.emailcode.outlook

import site.addzero.network.call.emailcode.imap.ImapEmailCodeProvider
import site.addzero.network.call.emailcode.imap.ImapMailboxPresets

class OutlookEmailCodeProvider : ImapEmailCodeProvider(
    id = ID,
    serverConfig = ImapMailboxPresets.OUTLOOK,
) {
    companion object {
        const val ID = "outlook"
    }
}
