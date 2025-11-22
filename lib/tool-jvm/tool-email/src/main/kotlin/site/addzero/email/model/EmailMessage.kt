package site.addzero.email.model

/**
 * 表示一封电子邮件消息的数据类
 *
 * @property from 发件人邮箱地址
 * @property to 收件人邮箱地址列表
 * @property cc 抄送邮箱地址列表
 * @property bcc 密送邮箱地址列表
 * @property subject 邮件主题
 * @property textContent 纯文本内容
 * @property htmlContent HTML格式内容
 * @property attachments 附件文件路径列表
 */
data class EmailMessage(
    val from: String,
    val to: List<String>,
    val cc: List<String> = emptyList(),
    val bcc: List<String> = emptyList(),
    val subject: String,
    val textContent: String? = null,
    val htmlContent: String? = null,
    val attachments: List<String> = emptyList()
)