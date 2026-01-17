package site.addzero.network.call.suno.example

import site.addzero.ksp.singletonadapter.anno.SingletonAdapter

@SingletonAdapter(inlineToParameters = true, inject = ["apiKey=env:TEST_KEY", "baseUrl=const:https://api.example.com"])
class InlineTestService(val apiKey: String, val baseUrl: String) {

    fun authenticateUser(username: String, password: String) {
        println("Authenticating user $username with API key: $apiKey, base URL: $baseUrl")
    }

    fun fetchUserData(userId: String) {
        println("Fetching data for user $userId using API key: $apiKey, base URL: $baseUrl")
    }

    fun sendNotification(message: String) {
        println("Sending notification: $message")
    }
}
