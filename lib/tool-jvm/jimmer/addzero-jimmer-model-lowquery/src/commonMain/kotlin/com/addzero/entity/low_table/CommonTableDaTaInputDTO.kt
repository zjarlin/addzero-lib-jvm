package com.addzero.entity.low_table

import kotlinx.serialization.Serializable

@Serializable
data class CommonTableDaTaInputDTO(
    val pageNo: Int = 1,
    val pageSize: Int = 10,
    //关键词
    val keyword: String,
    //排序条件
//       @Contextual
    val stateSorts: MutableSet<StateSort>,
    //查询条件
    val stateSearches: MutableSet<StateSearch>,
)
