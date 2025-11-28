package site.addzero.mybatis.mputil

/**
 * diffAndInter 方法的返回结果 - 前端和数据库的差集和交集
 *
 * @param P 实体类型
 * @property diff 差集 - 数据库中不存在的记录（需要新增）
 * @property inter 交集 - 数据库中已存在的记录（需要更新）
 * @author zjarlin
 */
data class DiffAndInterResult<P>(
    val diff: MutableList<P>,
    val inter: MutableList<P>
) {
    companion object {
        fun <P> empty(): DiffAndInterResult<P> = DiffAndInterResult(mutableListOf(), mutableListOf())
    }
}

/**
 * diffPairAndInter 方法的返回结果 - 前端和数据库的差集和交集（含配对）
 *
 * @param P 实体类型
 * @property diff 差集 - 数据库中不存在的记录
 * @property interPairs 交集配对 - 前端数据与数据库数据的配对
 * @author zjarlin
 */
data class DiffPairAndInterResult<P>(
    val diff: MutableList<P>,
    val interPairs: MutableList<InterPair<P>>
) {
    companion object {
        fun <P> empty(): DiffPairAndInterResult<P> = DiffPairAndInterResult(mutableListOf(), mutableListOf())
    }
}

/**
 * 交集配对 - 前端数据与数据库数据的对应关系
 *
 * @param P 实体类型
 * @property frontend 前端传入的数据
 * @property database 数据库中已存在的数据
 */
data class InterPair<P>(
    val frontend: P,
    val database: P
)
