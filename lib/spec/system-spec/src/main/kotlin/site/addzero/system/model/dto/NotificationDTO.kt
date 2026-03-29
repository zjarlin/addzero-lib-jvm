package site.addzero.system.model.dto

import site.addzero.system.model.common.PageQuery
import site.addzero.system.spi.notification.NotificationChannel
import site.addzero.system.spi.notification.NotificationPriority
import site.addzero.system.spi.notification.NotificationStatus
import java.time.Instant

/**
 * 通知数据传输对象
 */
data class NotificationDTO(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val channel: NotificationChannel,
    val priority: NotificationPriority,
    val status: NotificationStatus,
    val senderId: String?,          // 发送者ID，null为系统
    val senderName: String?,
    val actionUrl: String?,         // 点击跳转链接
    val actionType: String?,        // 业务类型
    val actionId: String?,          // 业务ID
    val extraData: Map<String, String>?, // 扩展数据
    val sentAt: Instant,
    val readAt: Instant?
)

/**
 * 接收者信息
 */
data class Recipient(
    val userId: String,
    val email: String? = null,
    val phone: String? = null,
    val pushToken: String? = null
)

data class NotificationSendRequest(
    val recipient: Recipient,
    val title: String,
    val content: String,
    val channel: NotificationChannel = NotificationChannel.IN_APP,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val actionUrl: String? = null,
    val actionType: String? = null,
    val actionId: String? = null,
    val extraData: Map<String, String>? = null,
    val senderId: String? = null,
    val senderName: String? = null,
    val scheduledAt: Instant? = null  // 定时发送
)

data class BroadcastRequest(
    val title: String,
    val content: String,
    val channel: NotificationChannel = NotificationChannel.IN_APP,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val targetUserIds: List<String>? = null,  // null表示全员
    val targetRoles: List<String>? = null,   // 按角色发送
    val actionUrl: String? = null,
    val extraData: Map<String, String>? = null
)

data class NotificationQuery(
    override val pageNum: Int = 1,
    override val pageSize: Int = 10,
    val userId: String? = null,
    val channel: NotificationChannel? = null,
    val status: NotificationStatus? = null,
    val priority: NotificationPriority? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null
) : PageQuery(pageNum, pageSize)

/**
 * 消息模板DTO
 */
data class MessageTemplateDTO(
    val id: String,
    val templateCode: String,
    val templateName: String,
    val channel: NotificationChannel,
    val titleTemplate: String,
    val contentTemplate: String,
    val description: String?,
    val isEnabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class TemplateCreateRequest(
    val templateCode: String,
    val templateName: String,
    val channel: NotificationChannel,
    val titleTemplate: String,
    val contentTemplate: String,
    val description: String? = null
)

data class TemplateUpdateRequest(
    val templateName: String? = null,
    val titleTemplate: String? = null,
    val contentTemplate: String? = null,
    val description: String? = null,
    val isEnabled: Boolean? = null
)
