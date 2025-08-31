package com.addzero.demo

import androidx.compose.runtime.Composable
import com.addzero.annotation.Route
import com.addzero.component.AddDataTable
import com.addzero.mock.mockkSysMunu


@Route
@Composable
fun daoisjdoaisjd(): Unit {
    val mockkSysMunu = mockkSysMunu()
    val values = mockkSysMunu.values
    val keys = mockkSysMunu.keys
    val data = values.toList()
    AddDataTable(
        data = data,
        columns = keys.toList(),
        getLabel = { it },
        getValue = {it.toString()},
    ) {

    }
}
