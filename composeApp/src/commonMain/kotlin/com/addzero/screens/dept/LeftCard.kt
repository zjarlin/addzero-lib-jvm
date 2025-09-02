package com.addzero.screens.dept

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.addzero.generated.isomorphic.SysDeptIso
import com.addzero.viewmodel.SysDeptViewModel

@Composable
fun LeftCard(
    vm: SysDeptViewModel, onNodeClick: (SysDeptIso) -> Unit = {
        vm.currentDeptVO = it
    }
) {
    Column {
        // 标题和添加按钮
        com.addzero.component.button.AddIconButton(text = "添加部门") { vm.showForm = true }

        com.addzero.component.search_bar.AddSearchBar(
            keyword = vm.keyword,
            onKeyWordChanged = { vm.keyword = it },
            onSearch = { vm.loadDeptTree() }
        )

        com.addzero.component.tree.AddTree(
            items = vm.deptVos,
            getId = { it.id!! },
            getLabel = { it.name },
            getChildren = { it.children },
            onNodeClick = onNodeClick,
        )


//                AddFlatTree(
//                    items = vm.deptVos,
//                    getId = { it.id!! },
//                    getParentId = { it.parent?.id },
//                    getName = { it.name.toString() },
//                    onNodeClick = {
//                        vm.currentDeptVO = it
//                    },
//                )
    }
}
