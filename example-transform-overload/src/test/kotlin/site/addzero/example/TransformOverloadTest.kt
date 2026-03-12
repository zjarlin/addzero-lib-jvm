package site.addzero.example

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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

        val expected = User(1L, "Test", "test@test.com")
        assertEquals(expected, userRepo.save(expected))

        val userInput: Input<User> = object : Input<User> {
            override fun toEntity(): User = expected
        }

        assertEquals(expected, userRepo.save(userInput))
    }

    @Test
    fun `test converters`() {
        val userInput: Input<User> = object : Input<User> {
            override fun toEntity(): User = User(1L, "Test", "test@test.com")
        }

        // 测试转换函数
        val user = userInput.toEntityInput()
        assertEquals(User(1L, "Test", "test@test.com"), user)
    }
}
