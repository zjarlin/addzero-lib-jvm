package site.addzero.network.call.emailcode.imap

import jakarta.mail.Address
import jakarta.mail.Flags
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.UIDFolder
import jakarta.mail.internet.InternetAddress
import jakarta.mail.search.HeaderTerm
import site.addzero.network.call.emailcode.AbstractPollingEmailCodeMailbox
import site.addzero.network.call.emailcode.model.EmailMailboxLoginRequest
import site.addzero.network.call.emailcode.model.EmailMailboxSecretType
import site.addzero.network.call.emailcode.model.EmailMessageDetail
import site.addzero.network.call.emailcode.model.EmailMessageSummary
import site.addzero.network.call.emailcode.model.MailAddress
import java.time.Instant
import java.util.Properties

class ImapEmailCodeMailbox(
    override val providerId: String,
    private val serverConfig: ImapServerConfig,
    private val loginRequest: EmailMailboxLoginRequest,
) : AbstractPollingEmailCodeMailbox() {
    override val address: String = loginRequest.address
    override val loginSecret: String = loginRequest.credential

    private val session: Session = Session.getInstance(buildProperties())
    private var store: Store? = null
    private var folder: Folder? = null

    override fun listMessages(page: Int, pageSize: Int): List<EmailMessageSummary> {
        require(page > 0) { "page must be > 0" }
        require(pageSize > 0) { "pageSize must be > 0" }

        val currentFolder = ensureFolder()
        val messageCount = currentFolder.messageCount
        if (messageCount <= 0) {
            return emptyList()
        }

        val newestIndex = messageCount - (page - 1) * pageSize
        if (newestIndex <= 0) {
            return emptyList()
        }

        val oldestIndex = maxOf(1, newestIndex - pageSize + 1)
        return currentFolder.getMessages(oldestIndex, newestIndex)
            .asList()
            .asReversed()
            .map { message ->
                EmailMessageSummary(
                    id = resolveMessageId(currentFolder, message),
                    fromAddress = message.fromAddresses().firstOrNull()?.address.orEmpty(),
                    fromName = message.fromAddresses().firstOrNull()?.name.orEmpty(),
                    subject = message.subject.orEmpty(),
                    previewText = "",
                    seen = message.flags.contains(Flags.Flag.SEEN),
                    createdAt = message.receivedDate?.toInstant() ?: message.sentDate?.toInstant(),
                )
            }
    }

    override fun getMessage(messageId: String): EmailMessageDetail {
        val currentFolder = ensureFolder()
        val message = resolveMessage(currentFolder, messageId)
            ?: throw IllegalArgumentException("IMAP message not found: $messageId")

        val content = extractContent(message)
        return EmailMessageDetail(
            id = resolveMessageId(currentFolder, message),
            fromAddress = message.fromAddresses().firstOrNull()?.address.orEmpty(),
            fromName = message.fromAddresses().firstOrNull()?.name.orEmpty(),
            to = message.recipients(Message.RecipientType.TO),
            subject = message.subject.orEmpty(),
            text = content.text,
            html = content.html,
            createdAt = message.receivedDate?.toInstant() ?: message.sentDate?.toInstant(),
        )
    }

    override fun close() {
        runCatching {
            folder?.takeIf { it.isOpen }?.close(false)
        }
        runCatching {
            store?.takeIf { it.isConnected }?.close()
        }
        folder = null
        store = null
    }

    private fun ensureFolder(): Folder {
        val connectedStore = store?.takeIf { it.isConnected } ?: session.getStore(serverConfig.protocol).also {
            connect(it)
            store = it
        }

        val openFolder = folder?.takeIf { it.isOpen } ?: connectedStore
            .getFolder(loginRequest.folderName ?: serverConfig.defaultFolderName)
            .also {
                it.open(Folder.READ_ONLY)
                folder = it
            }

        return openFolder
    }

    private fun connect(targetStore: Store) {
        targetStore.connect(
            serverConfig.host,
            serverConfig.port,
            loginRequest.address,
            loginRequest.credential,
        )
    }

    private fun buildProperties(): Properties =
        Properties().apply {
            put("mail.store.protocol", serverConfig.protocol)
            put("mail.${serverConfig.protocol}.host", serverConfig.host)
            put("mail.${serverConfig.protocol}.port", serverConfig.port.toString())
            put("mail.${serverConfig.protocol}.ssl.enable", serverConfig.sslEnabled.toString())
            put("mail.${serverConfig.protocol}.starttls.enable", serverConfig.startTlsEnabled.toString())
            put("mail.${serverConfig.protocol}.connectiontimeout", loginRequest.connectTimeoutMs.toString())
            put("mail.${serverConfig.protocol}.timeout", loginRequest.readTimeoutMs.toString())
            put("mail.${serverConfig.protocol}.writetimeout", loginRequest.writeTimeoutMs.toString())
            if (loginRequest.secretType == EmailMailboxSecretType.OAUTH2_ACCESS_TOKEN) {
                put("mail.${serverConfig.protocol}.auth.mechanisms", "XOAUTH2")
                put("mail.${serverConfig.protocol}.auth.login.disable", "true")
                put("mail.${serverConfig.protocol}.auth.plain.disable", "true")
            }
        }

    private fun resolveMessageId(folder: Folder, message: Message): String {
        val uid = (folder as? UIDFolder)?.getUID(message)
        if (uid != null && uid > 0) {
            return "uid:$uid"
        }
        val headerId = message.getHeader("Message-ID")?.firstOrNull()?.trim()
        if (!headerId.isNullOrBlank()) {
            return "mid:$headerId"
        }
        return "seq:${message.messageNumber}"
    }

    private fun resolveMessage(folder: Folder, messageId: String): Message? {
        return when {
            messageId.startsWith("uid:") && folder is UIDFolder -> {
                val uid = messageId.removePrefix("uid:").toLongOrNull() ?: return null
                folder.getMessageByUID(uid)
            }

            messageId.startsWith("mid:") -> {
                val headerValue = messageId.removePrefix("mid:")
                folder.search(HeaderTerm("Message-ID", headerValue)).firstOrNull()
            }

            messageId.startsWith("seq:") -> {
                val sequence = messageId.removePrefix("seq:").toIntOrNull() ?: return null
                folder.getMessage(sequence)
            }

            else -> null
        }
    }

    private fun extractContent(part: Part): ExtractedContent =
        when {
            part.isMimeType("text/plain") -> ExtractedContent(text = part.content?.toString().orEmpty())
            part.isMimeType("text/html") -> {
                val html = part.content?.toString().orEmpty()
                ExtractedContent(text = stripHtml(html), html = html)
            }

            part.isMimeType("multipart/*") -> {
                val multipart = part.content as? Multipart ?: return ExtractedContent()
                var content = ExtractedContent()
                repeat(multipart.count) { index ->
                    content = content.merge(extractContent(multipart.getBodyPart(index)))
                }
                content
            }

            part.isMimeType("message/rfc822") -> {
                val nested = part.content as? Part ?: return ExtractedContent()
                extractContent(nested)
            }

            else -> ExtractedContent()
        }

    private fun Message.fromAddresses(): List<MailAddress> =
        from?.map { it.toMailAddress() }.orEmpty()

    private fun Message.recipients(type: Message.RecipientType): List<MailAddress> =
        getRecipients(type)?.map { it.toMailAddress() }.orEmpty()

    private fun Address.toMailAddress(): MailAddress =
        when (this) {
            is InternetAddress -> MailAddress(address = address.orEmpty(), name = personal.orEmpty())
            else -> MailAddress(address = toString())
        }

    private fun stripHtml(html: String): String =
        html.replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private data class ExtractedContent(
        val text: String = "",
        val html: String = "",
    ) {
        fun merge(other: ExtractedContent): ExtractedContent =
            ExtractedContent(
                text = if (text.isNotBlank()) text else other.text,
                html = if (html.isNotBlank()) html else other.html,
            )
    }
}
