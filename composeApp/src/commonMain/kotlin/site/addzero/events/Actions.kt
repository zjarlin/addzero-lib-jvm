package site.addzero.events

sealed class Actions {
    data class Login(val username: String, val password: String) : Actions()
    data class LogOut(val username: String) : Actions()
}
