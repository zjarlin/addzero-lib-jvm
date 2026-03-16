# tool-api-payment

统一的支付二维码与订单查询 SPI，当前内置：

- 支付宝当面付默认实现
- 微信支付 Native Pay 默认实现

这个模块只保留最小接口，适合做“先接起来再往外包一层”的场景。

## 核心能力

- 生成支付二维码：入参只有 `orderName`、`totalAmount`
- 查询订单：入参只有 `orderNo`
- 通过 `ServiceLoader` 自动发现支付渠道实现
- 默认提供支付宝、微信两个 `PaymentProvider`

## 主要入口

```kotlin
import site.addzero.network.call.payment.spi.PaymentProviders

val alipay = PaymentProviders.load("alipay")
val wechat = PaymentProviders.load("wx")
```

### 生成二维码

```kotlin
val result = wechat.createQrCode(
    orderName = "测试订单",
    totalAmount = "12.30",
)

println(result.channel)
println(result.orderNo)
println(result.qrCode)
```

### 查询订单

```kotlin
val result = wechat.queryOrder("WX202603150001")

println(result.status)
println(result.totalAmount)
println(result.paidAmount)
println(result.platformTransactionNo)
```

## 返回结果

### `PaymentQrCodeResult`

- `channel`：支付渠道
- `orderNo`：商户订单号
- `orderName`：订单名称
- `totalAmount`：金额，单位元
- `qrCode`：二维码内容

### `PaymentOrderQueryResult`

- `channel`：支付渠道
- `orderNo`：商户订单号
- `status`：订单状态
- `totalAmount`：订单总金额
- `paidAmount`：实付金额
- `platformTransactionNo`：平台交易号
- `buyerId`：买家标识

## 环境变量

### 支付宝

- `ALIPAY_APP_ID`
- `ALIPAY_MERCHANT_PRIVATE_KEY`
- `ALIPAY_PUBLIC_KEY`
- `ALIPAY_NOTIFY_URL`

可选：

- `ALIPAY_GATEWAY_HOST`
- `ALIPAY_PROTOCOL`
- `ALIPAY_SIGN_TYPE`
- `ALIPAY_IGNORE_SSL`

### 微信支付

- `WECHAT_PAY_APP_ID`
- `WECHAT_PAY_MERCHANT_ID`
- `WECHAT_PAY_MERCHANT_SERIAL_NUMBER`
- `WECHAT_PAY_PRIVATE_KEY`
- `WECHAT_PAY_API_V3_KEY`
- `WECHAT_PAY_NOTIFY_URL`

可选：

- `WECHAT_PAY_CURRENCY`

## 设计约定

- `createQrCode(orderName, totalAmount)` 会由实现内部生成商户订单号
- `totalAmount` 使用元字符串，例如 `12.30`
- `queryOrder(orderNo)` 需要传入你自己保存下来的商户订单号
- 默认实现偏“直接接 SDK”，如果你要接业务系统，建议在外层再包一层应用服务

## 测试

当前已覆盖：

- 支付宝二维码生成
- 支付宝订单查询
- 微信二维码生成
- 微信订单查询
- SPI 默认实现发现

## 备注

- 这个模块目前默认不进入“小鳄鱼”文档站
- 如果后续要公开，再在 `docs/readme-collection.rules` 里单独放行
