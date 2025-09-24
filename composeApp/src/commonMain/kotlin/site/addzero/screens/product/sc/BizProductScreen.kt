package site.addzero.screens.product.sc

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import site.addzero.annotation.Route
import site.addzero.screens.product.DeviceScreen
import site.addzero.screens.product.ProductCategoryScreen
import site.addzero.screens.product.ProductScreen
import site.addzero.screens.product.vm.DeviceViewModel
import site.addzero.screens.product.vm.ProductCategoryViewModel
import site.addzero.screens.product.vm.ProductViewModel


@Route("物联网模块", "产品管理")
@Composable
fun ProdMannager(): Unit {
    val koinViewModel = koinViewModel<ProductViewModel>()
    ProductScreen(koinViewModel)
}

@Route("物联网模块", "产品分类管理")
@Composable
fun EquipmentClassificationManagement(): Unit {
    val koinViewModel = koinViewModel<ProductCategoryViewModel>()
    ProductCategoryScreen(koinViewModel)
}


@Route("物联网模块", "设备管理")
@Composable
fun DeviceMannager(): Unit {
    val koinViewModel = koinViewModel<DeviceViewModel>()
    DeviceScreen(koinViewModel)

}

@Route("物联网模块", "设备接入")
@Composable
fun shebiejieru(): Unit {
    Text("没写")
}
