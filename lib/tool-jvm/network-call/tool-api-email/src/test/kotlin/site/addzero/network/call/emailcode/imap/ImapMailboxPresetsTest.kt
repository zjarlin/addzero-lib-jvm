package site.addzero.network.call.emailcode.imap

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImapMailboxPresetsTest {
    @Test
    fun `gmail preset uses secure imap`() {
        assertEquals("imap.gmail.com", ImapMailboxPresets.GMAIL.host)
        assertEquals(993, ImapMailboxPresets.GMAIL.port)
        assertTrue(ImapMailboxPresets.GMAIL.sslEnabled)
    }

    @Test
    fun `outlook preset uses secure imap`() {
        assertEquals("outlook.office365.com", ImapMailboxPresets.OUTLOOK.host)
        assertEquals(993, ImapMailboxPresets.OUTLOOK.port)
        assertTrue(ImapMailboxPresets.OUTLOOK.sslEnabled)
    }
}
