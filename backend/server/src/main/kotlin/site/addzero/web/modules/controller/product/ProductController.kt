package site.addzero.web.modules.controller.product

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import site.addzero.model.entity.biz_device.Device
import site.addzero.model.entity.biz_device.Product
import site.addzero.model.entity.biz_device.ProductCategory
import site.addzero.model.entity.biz_device.ThingModel
import site.addzero.model.entity.biz_device.ThingModelProperty
import site.addzero.web.infra.jimmer.base.BaseController

@RestController
@RequestMapping("/device")
class DeviceController : BaseController<Device> {
}

@RestController
@RequestMapping("/product")
class ProductController : BaseController<Product> {
}

@RestController
@RequestMapping("/productCategory")
class ProductCategoryController : BaseController<ProductCategory>


@RestController
@RequestMapping("/thingModel")
class ThingModelController : BaseController<ThingModel> {}


@RestController
@RequestMapping("/thingModelProperty")
class ThingModelPropertyController : BaseController<ThingModelProperty> {}
