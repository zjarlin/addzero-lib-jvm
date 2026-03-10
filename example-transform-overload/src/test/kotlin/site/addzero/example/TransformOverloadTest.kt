package site.addzero.example

import org.junit.jupiter.api.Test

/**
 * 测试类 - 验证插件生成的重载方法
 */
class TransformOverloadTest {

    @Test
    fun `test repository with input types`() {
        // 创建模拟的 UserRepository 实现
        val userRepo = object : UserRepository {
            override fun save(entity: User): User {
                println("Saved: $entity")
                return entity
            }

            override fun saveAll(entities: Iterable<User>): List<User> {
                return entities.toList()
            }

            override fun saveList(entities: List<User>): List<User> {
                return entities
            }

            override fun findById(id: Long): User? = null
        }

      userRepo.save(User(1L, "Test", "test@test.com"))
        // 编译后，以下调用应该可以正常工作：
//         val userInput= StringInput(User(1L, "Test", "test@test.com"))
//         userRepo.save(userInput)  // 自动转换
    }

    @Test
    fun `test converters`() {
        val userInput: Input<User> = object : Input<User> {
            override fun toEntity(): User = User(1L, "Test", "test@test.com")
        }

        // 测试转换函数
        val user = userInput.toEntityInput()
        println("Converted: $user")
    }
}
