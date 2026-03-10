package site.addzero.example

/**
 * 示例程序入口
 *
 * 编译后，插件会自动生成以下重载方法：
 * - UserRepository.save(value: Input<User>)
 * - UserRepository.save(value: Draft<User>)
 * - UserRepository.saveAll(values: Iterable<Input<User>>)
 * - UserRepository.saveAllViaFromDraft(values: Iterable<Draft<User>>)
 * - UserRepository.saveList(values: List<Input<User>>)
 * - ProductRepository.save(value: Input<Product>)
 * - ProductRepository.save(value: Draft<Product>)
 * - processUserViaToEntityInput(entity: Input<User>)
 */
fun main() {
    println("Transform Overload Example")
    println("==========================")

    // 创建模拟实现
    val userRepo = object : UserRepository {
        override fun save(entity: User): User {
            println("Saving user: $entity")
            return entity
        }

        override fun saveAll(entities: Iterable<User>): List<User> {
            println("Saving ${entities.count()} users")
            return entities.toList()
        }

        override fun saveList(entities: List<User>): List<User> {
            println("Saving list of ${entities.size} users")
            return entities
        }

        override fun findById(id: Long): User? {
            return User(id, "User$id", "user$id@example.com")
        }
    }

    // 使用 Input 类型 - 编译后会自动调用转换
    val userInput: Input<User> = object : Input<User> {
        override fun toEntity(): User = User(1L, "Test", "test@example.com")
    }

    // 注意：以下代码需要插件编译后才能正常工作
    // 编译后，这些方法会被自动生成：
    // userRepo.save(userInput)  // 自动调用 userInput.toEntityInput() 转换

    println("\nGenerated methods after compilation:")
    println("- save(Input<User>) -> calls toEntityInput() internally")
    println("- save(Draft<User>) -> calls fromDraft() internally")
    println("- saveAllViaToEntityInput(Iterable<Input<User>>)")
    println("- saveAllViaFromDraft(Iterable<Draft<User>>)")
    println("- saveListViaToEntityInput(List<Input<User>>)")

    // 演示原始调用方式
    println("\nManual conversion (before plugin processing):")
    val user = userInput.toEntityInput()
    userRepo.save(user)
}
