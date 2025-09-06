package com.addzero.ui.infra

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.di.NavgationViewModel

/**
 * 渲染导航内容
 */
@Composable
context(navgationViewModel: NavgationViewModel )
fun MainContent() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        // 渲染导航内容
        val navController = navgationViewModel.getNavController()
        navgationViewModel.Initialize(navController)
    }
}

