package site.addzero.network.call.tempmail

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration")
@Disabled
class TempMailClientTest {
    private val client = TempMailClient()

    @Test
    fun `should get available domains`() {
        val domains = client.getDomains()

        assertFalse(domains.isEmpty())
        assertNotNull(domains.first().domain)
    }

  /**
   * TempMailMailbox(address=itncxqb1yx@dollicons.com, password=z8bfimbbyhc1, accountId=69a107f62b5c802d51079a7e, token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3NzIxNjEwMTQsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJhZGRyZXNzIjoiaXRuY3hxYjF5eEBkb2xsaWNvbnMuY29tIiwiaWQiOiI2OWExMDdmNjJiNWM4MDJkNTEwNzlhN2UiLCJtZXJjdXJlIjp7InN1YnNjcmliZSI6WyIvYWNjb3VudHMvNjlhMTA3ZjYyYjVjODAyZDUxMDc5YTdlIl19fQ.cFgWmLh-O28OVoP8VGoM5U8njWV7V9VI6kEoVD4ZsVu6BkwN_PN8Az5m4UqUi7NubjPq_7_hKrlGuCp7kgKCOg)
   *
   *
   */
    @Test
    fun `should create mailbox and query inbox`() {
        val mailbox = client.createMailboxAndLogin(prefix = "it", passwordLength = 12)

        assertFalse(mailbox.address.isBlank())
        assertFalse(mailbox.password.isBlank())
        assertFalse(mailbox.accountId.isBlank())
        assertFalse(mailbox.token.isBlank())

        val messages = client.listMessages(mailbox.token)
        assertNotNull(messages)

        if (messages.isNotEmpty()) {
            val detail = client.getMessage(mailbox.token, messages.first().id)
            assertEquals(messages.first().id, detail.id)
        }
    }
  @Test
  fun `listmsg`() {

    val messages = client.listMessages("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3NzIxNjEwMTQsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJhZGRyZXNzIjoiaXRuY3hxYjF5eEBkb2xsaWNvbnMuY29tIiwiaWQiOiI2OWExMDdmNjJiNWM4MDJkNTEwNzlhN2UiLCJtZXJjdXJlIjp7InN1YnNjcmliZSI6WyIvYWNjb3VudHMvNjlhMTA3ZjYyYjVjODAyZDUxMDc5YTdlIl19fQ.cFgWmLh-O28OVoP8VGoM5U8njWV7V9VI6kEoVD4ZsVu6BkwN_PN8Az5m4UqUi7NubjPq_7_hKrlGuCp7kgKCOg")
    println()
  }
}
