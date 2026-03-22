package site.addzero.jimmer.ktx.serializer

data class JimmerKtxSerializerConfig(
  val ignoreUnknownProperties: Boolean = true,
  val ignoreReadonlyProperties: Boolean = true,
  val skipInvisibleProperties: Boolean = true,
)
