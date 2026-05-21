package site.addzero.network.call.emailcode.spi

import java.util.ServiceLoader

object EmailCodeProviders {
    fun all(classLoader: ClassLoader = EmailCodeProvider::class.java.classLoader): List<EmailCodeProvider> =
        ServiceLoader.load(EmailCodeProvider::class.java, classLoader)
            .iterator()
            .asSequence()
            .toList()

    fun firstOrNull(id: String, classLoader: ClassLoader = EmailCodeProvider::class.java.classLoader): EmailCodeProvider? =
        all(classLoader).firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun require(id: String, classLoader: ClassLoader = EmailCodeProvider::class.java.classLoader): EmailCodeProvider =
        firstOrNull(id, classLoader)
            ?: throw IllegalArgumentException(
                "EmailCodeProvider $id not found. Available providers: ${
                    all(classLoader).joinToString { it.id }.ifBlank { "<none>" }
                }",
            )
}
