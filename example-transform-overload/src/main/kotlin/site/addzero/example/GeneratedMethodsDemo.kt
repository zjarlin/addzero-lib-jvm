package site.addzero.example

/**
 * 演示实际调用插件生成的重载方法
 */
fun demoGeneratedMethods() {
    println("=== 演示生成的重载方法 ===")

    // 创建 UserRepository 的实现类
    val userRepo = UserRepositoryImpl()

    // 创建 Input 和 Draft 实例
    val userInput: Input<User> = InputImpl(User(1L, "InputUser", "input@test.com"))
    val userDraft: Draft<User> = DraftImpl(User(2L, "DraftUser", "draft@test.com"))

    println("\n1. 调用生成的 save(Input<User>) 方法:")
    val savedFromInput = userRepo.save(userInput)
    println("   结果: $savedFromInput")

    println("\n2. 调用生成的 save(Draft<User>) 方法:")
    val savedFromDraft = userRepo.save(userDraft)
    println("   结果: $savedFromDraft")

    println("\n3. 调用生成的 saveViaToEntityInput 方法:")
    val savedViaToEntityInput = userRepo.saveViaToEntityInput(userInput)
    println("   结果: $savedViaToEntityInput")

    println("\n4. 调用生成的 saveAllViaToEntityInput 方法:")
    val savedList = userRepo.saveAllViaToEntityInput(listOf(userInput, userInput))
    println("   结果: $savedList")

    println("\n5. 调用生成的 saveAllViaFromDraft 方法:")
    val savedListFromDraft = userRepo.saveAllViaFromDraft(listOf(userDraft))
    println("   结果: $savedListFromDraft")

    // 测试顶层函数生成的重载
    println("\n6. 调用生成的顶层函数 processUserViaToEntityInput:")
    val result = processUserViaToEntityInput(userInput)
    println("   结果: $result")

    // 如果存在 Converters 中的转换方法
    println("\n7. 调用生成的 saveAllViaToEntityViaConverters 方法:")
    val savedViaConverters = userRepo.saveAllViaToEntityViaConverters(listOf(userInput))
    println("   结果: $savedViaConverters")
}

/**
 * UserRepository 的具体实现类
 * 注意：不需要实现生成的重载方法，它们作为 default 方法已存在
 */
class UserRepositoryImpl : UserRepository {
    override fun save(entity: User): User {
        println("   [save(User)] 被调用，entity=$entity")
        return entity
    }

    override fun saveAll(entities: Iterable<User>): List<User> {
        println("   [saveAll(Iterable<User>)] 被调用，count=${entities.count()}")
        return entities.toList()
    }

    override fun saveList(entities: List<User>): List<User> {
        println("   [saveList(List<User>)] 被调用，size=${entities.size}")
        return entities
    }

    override fun findById(id: Long): User? {
        println("   [findById(Long)] 被调用，id=$id")
        return User(id, "User$id", "user$id@test.com")
    }
}

/**
 * Input 实现类
 */
class InputImpl(private val user: User) : Input<User> {
    override fun toEntity(): User {
        println("   [Input.toEntity()] 被调用")
        return user
    }
}

/**
 * Draft 实现类
 */
class DraftImpl(private val user: User) : Draft<User> {
    override fun toEntity(): User {
        println("   [Draft.toEntity()] 被调用")
        return user
    }
}

/**
 * 扩展 main 函数来运行演示
 */
fun main() {
    demoGeneratedMethods()
}
