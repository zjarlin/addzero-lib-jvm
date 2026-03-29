package site.addzero.system.spi.notification

import site.addzero.system.model.common.PageResult
import site.addzero.system.model.dto.BroadcastRequest
import site.addzero.system.model.dto.MessageTemplateDTO
import site.addzero.system.model.dto.NotificationDTO
import site.addzero.system.model.dto.NotificationQuery
import site.addzero.system.model.dto.NotificationSendRequest
import site.addzero.system.model.dto.Recipient
import site.addzero.system.model.dto.TemplateCreateRequest
import site.addzero.system.model.dto.TemplateUpdateRequest

/**
 * 通知服务SPI
 * 提供站内信、短信、邮件等通知渠道的统一接口
 */
interface NotificationSpi {

    /**
     * 发送通知
     * @param request 发送请求
     * @return 通知ID
     */
    fun send(request: NotificationSendRequest): String

    /**
     * 批量发送通知
     */
    fun sendBatch(requests: List<NotificationSendRequest>): List<String>

    /**
     * 发送模板通知
     */
    fun sendWithTemplate(templateCode: String, params: Map<String, Any?>, recipient: Recipient): String

    /**
     * 根据ID获取通知
     */
    fun getById(id: String): NotificationDTO?

    /**
     * 查询用户的通知列表
     */
    fun getByUserId(userId: String, status: NotificationStatus? = null): List<NotificationDTO>

    /**
     * 分页查询通知
     */
    fun page(query: NotificationQuery): PageResult<NotificationDTO>

    /**
     * 标记通知为已读
     */
    fun markAsRead(id: String)

    /**
     * 批量标记已读
     */
    fun markBatchAsRead(ids: List<String>)

    /**
     * 删除通知
     */
    fun delete(id: String)

    /**
     * 获取未读通知数量
     */
    fun getUnreadCount(userId: String): Int

    /**
     * 发送公告（全员通知）
     */
    fun broadcast(request: BroadcastRequest): String
}

/**
 * 消息模板服务SPI
 */
interface MessageTemplateSpi {

    /**
     * 创建模板
     */
    fun create(request: TemplateCreateRequest): MessageTemplateDTO

    /**
     * 更新模板
     */
    fun update(templateCode: String, request: TemplateUpdateRequest): MessageTemplateDTO

    /**
     * 根据编码获取模板
     */
    fun getByCode(templateCode: String): MessageTemplateDTO?

    /**
     * 删除模板
     */
    fun delete(templateCode: String)

    /**
     * 渲染模板
     */
    fun render(templateCode: String, params: Map<String, Any?>): RenderedMessage
}

enum class NotificationChannel {
    IN_APP,     // 站内信
    EMAIL,      // 邮件
    SMS,        // 短信
    PUSH,       // 推送
    WECHAT,     // 微信
    DINGTALK    // 钉钉
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class NotificationStatus {
    UNREAD,
    READ,
    ARCHIVED
}

data class RenderedMessage(
    val title: String,
    val content: String,
    val channel: NotificationChannel
)
