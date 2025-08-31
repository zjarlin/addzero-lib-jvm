package com.addzero.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun AddEditDeleteButton(
    showDelete: Boolean = true,
    showEdit: Boolean = true,
    onEditClick: () -> Unit, onDeleteClick: () -> Unit,
    renderCustomActions: @Composable () -> Unit = {},
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
    ) {


        if (showEdit) {
            _root_ide_package_.com.addzero.component.button.AddIconButton(text = "编辑", imageVector = Icons.Default.Edit, onClick = onEditClick)

        }

        if (showDelete) {

            _root_ide_package_.com.addzero.component.button.AddDeleteButton { onDeleteClick() }
        }

        renderCustomActions()
    }


}
