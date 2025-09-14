package site.addzero.screens.dept

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import site.addzero.generated.isomorphic.SysDeptIso
import site.addzero.viewmodel.SysDeptViewModel

@Composable
fun LeftCard(
    vm: SysDeptViewModel, onNodeClick: (SysDeptIso) -> Unit = {
        vm.currentDeptVO = it
    }
) {
    Column {
        // 标题和添加按钮
        site.addzero.component.button.AddIconButton(text = "添加部门") { vm.showForm = true }

        site.addzero.component.search_bar.AddSearchBar(
            keyword = vm.keyword,
            onKeyWordChanged = { vm.keyword = it },
            onSearch = { vm.loadDeptTree() }
        )

        site.addzero.component.tree.AddTree(
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
