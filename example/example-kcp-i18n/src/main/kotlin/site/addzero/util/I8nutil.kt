package site.addzero.util

object I8nutil {
    private val translations = mapOf(
        "Messages_helloMessage_text_你好" to "hello",
        "Messages_farewellMessage_text_再见" to "goodbye",
    )

    fun t(key: String): String {
        return translations[key] ?: key
    }
}
