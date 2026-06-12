package site.addzero.configcenter

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable

class ConfigCenterIntegrationTest {
  @Serializable
  data class FeatureSwitch(
    val enabled: Boolean,
    val ratio: Int,
  )

  @Test
  fun setAndGetAgainstRemoteConfigCenter() = runTest {
    val password = configCenterTestPassword() ?: return@runTest
    val suffix = Random.nextInt(1_000_000, 9_999_999)
    val instance = ConfigCenter(TEST_BASE_URL)
      .login(TEST_USERNAME, password)
      .checkoutNamespace("cmp-aio.dev")

    val textKey = "sdk.common.text.$suffix"
    val numberKey = "sdk.common.number.$suffix"
    val booleanKey = "sdk.common.boolean.$suffix"
    val objectKey = "sdk.common.object.$suffix"
    val keys = listOf(textKey, numberKey, booleanKey, objectKey)

    try {
      instance.set(textKey, "hello-$suffix", "commonTest remote text")
      instance.set(numberKey, 42, "commonTest remote number")
      instance.set(booleanKey, true, "commonTest remote boolean")
      instance.set(objectKey, FeatureSwitch(enabled = true, ratio = 80), "commonTest remote object")

      assertEquals("hello-$suffix", instance.get<String>(textKey))
      assertEquals(42, instance.get<Int>(numberKey))
      assertEquals(true, instance.get<Boolean>(booleanKey))
      assertEquals(FeatureSwitch(enabled = true, ratio = 80), instance.get<FeatureSwitch>(objectKey))
    } finally {
      keys.forEach { key -> runCatching { instance.delete(key) } }
    }
  }

  @Test
  fun devNamespaceFallsBackToCommonNamespace() = runTest {
    val password = configCenterTestPassword() ?: return@runTest
    val suffix = Random.nextInt(1_000_000, 9_999_999)
    val common = ConfigCenter(TEST_BASE_URL)
      .login(TEST_USERNAME, password)
      .checkoutNamespace("cmp-aio.common")
    val dev = ConfigCenter(TEST_BASE_URL)
      .login(TEST_USERNAME, password)
      .checkoutNamespace("cmp-aio.dev")
    val key = "sdk.common.fallback.$suffix"

    try {
      common.set(key, "common-$suffix", "commonTest common fallback")

      assertEquals("cmp-aio.common", dev.commonNamespace)
      assertEquals("common-$suffix", dev.get<String>(key))
      assertEquals("cmp-aio.common", assertNotNull(dev.getItem(key)).namespace)

      dev.set(key, "dev-$suffix", "commonTest dev override")

      assertEquals("dev-$suffix", dev.get<String>(key))
      assertEquals("cmp-aio.dev", assertNotNull(dev.getItem(key)).namespace)
      assertEquals("common-$suffix", common.get<String>(key))
    } finally {
      runCatching { dev.delete(key) }
      runCatching { common.delete(key) }
    }
  }
}

private const val TEST_BASE_URL = "http://config.addzero.site"
private const val TEST_USERNAME = "zjarlin"

internal expect fun configCenterTestPassword(): String?
