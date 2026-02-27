# tool-api-temp-mail

基于 [mail.tm](https://docs.mail.tm/) 的临时邮箱调用库，支持：

- 获取可用域名
- 创建临时邮箱账号
- 获取登录 token
- 查询收件箱
- 查看邮件详情

## API

主要入口：`TempMailClient`

- `getDomains()`
- `createMailboxAndLogin(prefix, passwordLength)`
- `createAccount(address, password)`
- `createToken(address, password)`
- `listMessages(token, page)`
- `getMessage(token, messageId)`

## 示例

```kotlin
import site.addzero.network.call.tempmail.TempMailClient

fun main() {
    val client = TempMailClient()

    val mailbox = client.createMailboxAndLogin(prefix = "demo")
    println("address=${mailbox.address}")
    println("token=${mailbox.token}")

    val messages = client.listMessages(mailbox.token)
    println("inbox count=${messages.size}")

    if (messages.isNotEmpty()) {
        val detail = client.getMessage(mailbox.token, messages.first().id)
        println("subject=${detail.subject}")
        println("text=${detail.text}")
    }
}
```

## 注意

1. 本库依赖第三方服务 mail.tm，稳定性受外部网络影响。
2. 临时邮箱服务通常有频率限制，请自行控制调用频率。
3. 临时邮箱仅建议用于测试场景，不用于生产核心账号体系。
