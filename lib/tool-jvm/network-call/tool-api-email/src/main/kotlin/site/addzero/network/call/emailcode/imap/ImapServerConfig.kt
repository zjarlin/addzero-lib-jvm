package site.addzero.network.call.emailcode.imap

data class ImapServerConfig(
    val host: String,
    val port: Int = 993,
    val protocol: String = "imap",
    val sslEnabled: Boolean = true,
    val startTlsEnabled: Boolean = false,
    val defaultFolderName: String = "INBOX",
)
