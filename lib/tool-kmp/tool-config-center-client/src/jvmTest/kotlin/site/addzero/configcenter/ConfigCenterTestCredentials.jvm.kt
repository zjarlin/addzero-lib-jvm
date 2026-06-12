package site.addzero.configcenter

internal actual fun configCenterTestPassword(): String? =
  System.getProperty("config.center.test.password")?.takeIf(String::isNotBlank)
    ?: System.getenv("CONFIG_CENTER_TEST_PASSWORD")?.takeIf(String::isNotBlank)
