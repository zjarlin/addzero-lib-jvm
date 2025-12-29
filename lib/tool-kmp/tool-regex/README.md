# Tool Regex (正则表达式工具库)

提供常用的正则表达式验证枚举，简化表单验证逻辑。

## 功能

- 手机号、身份证、邮箱验证
- 密码强度验证
- 用户名、中文姓名验证
- 数字、金额格式验证
- 日期时间格式验证
- URL、IP、MAC 地址验证
- 文件格式验证
- 其他常用验证（邮编、银行卡、QQ、微信号等）

## 使用方法

```kotlin
dependencies {
    implementation("site.addzero:tool-regex:+")
}
```

## 示例

### 基础验证

```kotlin
import site.addzero.regex.RegexEnum

// 验证手机号
val isValid = RegexEnum.PHONE.matches("13800138000")

// 验证并获取错误信息
val (valid, message) = RegexEnum.validate("test@example.com", RegexEnum.EMAIL)
if (!valid) {
    println(message) // 输出错误提示
}
```

### 支持的验证类型

**联系方式**
- `PHONE` - 手机号
- `EMAIL` - 邮箱
- `QQ` - QQ号
- `WECHAT` - 微信号

**身份信息**
- `ID_CARD` - 身份证号
- `CHINESE_NAME` - 中文姓名
- `USERNAME` - 用户名
- `BANK_CARD` - 银行卡号
- `ZIP_CODE` - 邮政编码

**密码**
- `PASSWORD` - 普通密码（8-16位，包含字母和数字）
- `STRONG_PASSWORD` - 强密码（包含大小写字母、数字和特殊字符）

**数字**
- `INTEGER` - 整数
- `POSITIVE_INTEGER` - 正整数
- `DECIMAL` - 小数
- `MONEY` - 金额格式

**日期时间**
- `DATE` - 日期（yyyy-MM-dd）
- `TIME` - 时间（HH:mm:ss）
- `TIME_MINUTE` - 时间（HH:mm）
- `DATETIME` - 日期时间（yyyy-MM-dd HH:mm:ss）
- `DATETIME_MINUTE` - 日期时间（yyyy-MM-dd HH:mm）

**网络**
- `URL` - URL地址
- `IP_V4` - IPv4地址
- `MAC` - MAC地址

**文件**
- `IMAGE` - 图片格式
- `VIDEO` - 视频格式
- `AUDIO` - 音频格式
- `DOCUMENT` - 文档格式

**其他**
- `CHINESE` - 纯中文
- `ENGLISH` - 纯英文字母
