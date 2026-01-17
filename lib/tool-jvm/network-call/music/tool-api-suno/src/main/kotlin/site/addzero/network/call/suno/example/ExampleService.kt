package site.addzero.network.call.suno.example

import site.addzero.ksp.singletonadapter.anno.ExtractCommonParameters

@ExtractCommonParameters
class ExampleService {

    fun authenticateUser(apiKey: String, baseUrl: String, username: String, password: String) {
        // Authenticate user logic
        println("Authenticating user $username with API key and base URL")
    }

    fun fetchUserData(apiKey: String, baseUrl: String, userId: String) {
        // Fetch user data logic
        println("Fetching data for user $userId using API key and base URL")
    }

    fun updateUserProfile(apiKey: String, baseUrl: String, userId: String, profileData: Map<String, Any>) {
        // Update profile logic
        println("Updating profile for user $userId with API key and base URL")
    }

    fun deleteUser(apiKey: String, baseUrl: String, userId: String, confirmation: Boolean) {
        // Delete user logic
        println("Deleting user $userId with API key and base URL")
    }

    fun sendNotification(timeout: Int, retryCount: Int, message: String) {
        // Send notification logic
        println("Sending notification with timeout and retry settings")
    }

    fun checkHealth(timeout: Int, retryCount: Int) {
        // Health check logic
        println("Performing health check with timeout and retry settings")
    }
}
