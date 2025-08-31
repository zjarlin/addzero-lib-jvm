package com.addzero.component.table.model

import com.addzero.entity.low_table.SpecPageResult
import com.addzero.entity.low_table.StateSearch
import com.addzero.entity.low_table.StateSort

interface BaseCrudApi<T> {

    suspend fun <T> queryPage(
        sortStats: MutableSet<StateSort>,
        stateSearchConditions: MutableSet<StateSearch>,
        pageNo: Int = 1,
        pageSize: Int = 10,
    ): SpecPageResult<T>

    suspend fun batchDelete(_selectedItemIds: Set<Any>): Boolean
    suspend fun batchExport(
        _sortState: MutableSet<StateSort>,
        toMutableSet: MutableSet<StateSearch>,
        _selectedItemIds: Set<Any>
    );
}
