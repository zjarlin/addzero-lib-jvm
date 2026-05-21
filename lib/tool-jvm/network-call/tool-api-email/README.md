# tool-api-email

Unified email verification-code access for temporary mailboxes and user-owned inboxes.

- Maven coordinate: `site.addzero:tool-api-email`
- Local module path: `lib/tool-jvm/network-call/tool-api-email`
- Runtime: Kotlin/JVM, OkHttp, Jakarta Mail (IMAP)

## What it provides

- A provider-neutral SPI for email-based verification-code retrieval
- `mail.tm` mailbox creation and inbox polling
- Generic IMAP inbox polling
- Gmail and Outlook IMAP presets built on top of the generic IMAP provider

## Minimal usage

```kotlin
import site.addzero.network.call.emailcode.model.EmailCodeRequest
import site.addzero.network.call.emailcode.model.EmailMailboxLoginRequest
import site.addzero.network.call.emailcode.model.EmailMailboxSecretType
import site.addzero.network.call.emailcode.spi.EmailCodeProviders

val gmail = EmailCodeProviders.require("gmail")
gmail.login(
  EmailMailboxLoginRequest(
    address = "you@gmail.com",
    credential = "<oauth-access-token-or-app-password>",
    secretType = EmailMailboxSecretType.OAUTH2_ACCESS_TOKEN,
  ),
).use { mailbox ->
  val code = mailbox.awaitCode(
    EmailCodeRequest(
      senderIncludes = listOf("no-reply@example.com"),
      subjectIncludes = listOf("verification"),
    ),
  )
  println(code.code)
}
```

## Provider notes

- `mail.tm`: supports mailbox creation and login to previously created mailboxes.
- `gmail`: IMAP preset for `imap.gmail.com:993`.
- `outlook`: IMAP preset for `outlook.office365.com:993`.
- If you need a different IMAP host or folder, instantiate `ImapEmailCodeProvider` directly.
