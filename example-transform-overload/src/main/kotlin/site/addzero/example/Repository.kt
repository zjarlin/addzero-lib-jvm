package site.addzero.example

import site.addzero.kcp.transformoverload.annotations.GenerateTransformOverloads

/**
 * 示例 Repository 接口
 * 使用 @GenerateTransformOverloads 标记后，插件会自动生成接受 Input/Draft 的重载方法
 */
@GenerateTransformOverloads
interface UserRepository {
    /**
     * 保存单个实体
     * 插件会生成: save(value: Input<User>) 和 save(value: Draft<User>)
     */
    fun save(entity: User): User

    /**
     * 保存多个实体
     * 插件会生成: saveAll(values: Iterable<Input<User>>) 等方法
     */
    fun saveAll(entities: Iterable<User>): List<User>

    /**
     * 保存列表
     */
    fun saveList(entities: List<User>): List<User>

    /**
     * 查找用户
     */
    fun findById(id: Long): User?
}

/**
 * 方法级别标记示例
 */
interface ProductRepository {
    @GenerateTransformOverloads
    fun save(entity: Product): Product

    // 这个方法不会被生成重载
    fun delete(entity: Product): Boolean
}

/**
 * 顶层函数标记示例
 */
@GenerateTransformOverloads
fun processUser(entity: User): String {
    return "Processing ${entity.name}"
}
