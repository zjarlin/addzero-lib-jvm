package site.addzero.entity.low_table
data class CommonTableDaTaInputDTO(
    val pageNo: Int = 1,
    val pageSize: Int = 10,
    val stateSorts: MutableSet<StateSort>,
    val stateSearches: MutableSet<StateSearch>,
)
