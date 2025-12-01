package site.addzero.mybatis.mputil

/**
 * compareSaveOrUpdate 方法的返回结果
 *
 * @param P 实体类型
 * @property toInsert 要新增的对象列表
 * @property toUpdate 要修改的对象列表
 * @property insertSuccess 新增操作是否成功
 * @property updateSuccess 修改操作是否成功
 * @author zjarlin
 */
data class CompareSaveOrUpdateResult<P>(
    val toInsert: MutableList<P>,
    val toUpdate: MutableList<P>,
    val insertSuccess: Boolean,
    val updateSuccess: Boolean
) {
    val allSuccess: Boolean
        get() = insertSuccess && updateSuccess

    val anySuccess: Boolean
        get() = insertSuccess || updateSuccess

    val mergedResult: List<P>
        get() = listOfNotNull(toInsert, toUpdate).flatten()

    companion object {
        fun <P> empty() = CompareSaveOrUpdateResult(mutableListOf<P>(), mutableListOf(),
            insertSuccess = false,
            updateSuccess = false
        )
    }
}
