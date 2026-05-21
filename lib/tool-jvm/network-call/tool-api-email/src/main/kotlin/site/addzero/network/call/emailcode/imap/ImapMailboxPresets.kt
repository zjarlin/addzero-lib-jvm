package site.addzero.network.call.emailcode.imap

object ImapMailboxPresets {
    val GMAIL = ImapServerConfig(
        host = "imap.gmail.com",
        port = 993,
        sslEnabled = true,
        startTlsEnabled = false,
        defaultFolderName = "INBOX",
    )

    val OUTLOOK = ImapServerConfig(
        host = "outlook.office365.com",
        port = 993,
        sslEnabled = true,
        startTlsEnabled = false,
        defaultFolderName = "INBOX",
    )
}
