package site.addzero.core.network.token

import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single

@Single
class TokenManager(
  private val settings: Settings
) {
  fun setToken(token: String) {
    settings.putString("token", token)
  }

  fun getToken(): String? {
    return settings.getStringOrNull("token")
  }

  fun clearToken() {
    settings.remove("token")
  }

}
