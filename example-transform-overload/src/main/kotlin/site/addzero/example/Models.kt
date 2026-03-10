package site.addzero.example

/**
 * 用户实体
 */
data class User(
    val id: Long,
    val name: String,
    val email: String
)

/**
 * 产品实体
 */
data class Product(
    val id: Long,
    val name: String,
    val price: Double
)
