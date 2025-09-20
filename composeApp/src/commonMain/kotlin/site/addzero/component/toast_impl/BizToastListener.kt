package site.addzero.component.toast_impl

import androidx.compose.runtime.Composable
import site.addzero.component.toast.AddToastListener
import site.addzero.exp.BizException
import site.addzero.ioc.annotation.Bean


@Composable
@Bean
fun BizToastListener(): Unit {
    AddToastListener()
}
