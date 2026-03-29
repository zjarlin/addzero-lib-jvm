# system-spec 模块

后台管理系统基础业务的标准化SPI接口与默认实现。

## 设计原则

- **SPI优先**：所有功能先定义接口，提供解耦的扩展点
- **默认实现**：提供基于内存的默认实现，适用于开发测试
- **即插即用**：业务模块可通过接口替换为持久化实现，无需改动业务代码

## 模块结构

```
site.addzero.system/
├── common/                 # 通用DTO与异常
│   ├── dto/               # 分页、通用响应
│   └── exception/         # 模块异常
│
├── rbac/                  # 权限控制
│   ├── dto/               # RoleDTO, PermissionDTO, UserRoleDTO
│   ├── spi/               # RoleSpi, PermissionSpi, UserRoleSpi, PermissionCheckSpi
│   └── feature/           # InMemory实现
│
├── fileupload/            # 文件上传
│   ├── dto/               # FileStorageResult, FileRecordDTO
│   ├── spi/               # FileStorageSpi, FileRecordSpi
│   └── feature/           # LocalFileStorage, InMemoryRecord
│
├── audit/                 # 审计日志
│   ├── dto/               # AuditLogDTO
│   ├── spi/               # AuditLogSpi
│   └── feature/           # InMemoryAuditLog
│
├── config/                # 系统配置
│   ├── dto/               # ConfigDTO
│   ├── spi/               # SystemConfigSpi
│   └── feature/           # InMemoryConfig
│
└── notification/          # 通知服务
    ├── dto/               # NotificationDTO, MessageTemplateDTO
    ├── spi/               # NotificationSpi, MessageTemplateSpi
    └── feature/           # InMemory实现
```

## 快速开始

### 1. 使用默认配置

```kotlin
import site.addzero.system.SystemModuleConfig
import site.addzero.system.systemModule

// 方式一：全部使用默认内存实现
val config = SystemModuleConfig.createDefault(storagePath = "/data/uploads")

// 方式二：部分自定义
val customConfig = systemModule {
    configSpi = MyDatabaseConfigService()
    // 其他保持默认
}
```

### 2. RBAC 权限控制

```kotlin
val roleSpi = config.roleSpi
val permissionSpi = config.permissionSpi
val permissionCheck = config.permissionCheckSpi

// 创建角色
val adminRole = roleSpi.create(RoleCreateRequest(
    code = "ADMIN",
    name = "管理员",
    description = "系统管理员"
))

// 创建权限
val userRead = permissionSpi.create(PermissionCreateRequest(
    code = "user:read",
    name = "用户查看",
    resourceType = ResourceType.API,
    action = ActionType.READ
))

// 绑定权限到角色
roleSpi.assignPermissions(adminRole.id, setOf(userRead.id))

// 为用户分配角色
userRoleSpi.assignRoles("user123", setOf(adminRole.id))

// 权限检查
if (permissionCheck.hasPermission("user123", "user:read")) {
    // 有权限，执行操作
}
```

### 3. 文件上传

```kotlin
val storageSpi = config.fileStorageSpi
val recordSpi = config.fileRecordSpi

// 存储文件
val inputStream = File("avatar.png").inputStream()
val result = storageSpi.store(inputStream, FileUploadRequest(
    filename = "avatar.png",
    contentType = "image/png",
    bucket = "avatars",
    uploaderId = "user123"
))

// 创建记录
val record = recordSpi.create(FileRecordCreateRequest(
    fileId = result.fileId,
    originalFilename = result.originalFilename,
    storagePath = result.storagePath,
    storageType = result.storageType,
    bucket = "avatars",
    size = result.size,
    uploaderId = "user123",
    bizType = "user",
    bizId = "user123"
))

// 获取文件
val fileStream = storageSpi.retrieve(result.fileId)
```

### 4. 审计日志

```kotlin
val auditSpi = config.auditLogSpi

// 记录操作
auditSpi.record(AuditOperationRequest(
    userId = "user123",
    username = "admin",
    operationType = OperationType.CREATE,
    operationModule = "user",
    operationAction = "createUser",
    description = "创建新用户",
    bizType = "user",
    bizId = "user456",
    durationMs = 150
))

// 查询日志
val logs = auditSpi.page(AuditLogQuery(
    userId = "user123",
    operationType = OperationType.CREATE
))
```

### 5. 系统配置

```kotlin
val configSpi = config.configSpi

// 设置配置
configSpi.set("app.name", "MyApp")
configSpi.set("app.version", "1.0.0")

// 获取配置
val appName = configSpi.getString("app.name")
val maxUpload = configSpi.getInt("file.max_size", 10485760)
val enabled = configSpi.getBoolean("feature.x.enabled", false)

// 按分类获取
val systemConfigs = configSpi.listByCategory("system")
```

### 6. 通知服务

```kotlin
val notifySpi = config.notificationSpi

// 发送通知
val notifId = notifySpi.send(NotificationSendRequest(
    recipient = Recipient(userId = "user123"),
    title = "系统通知",
    content = "您的订单已发货",
    channel = NotificationChannel.IN_APP,
    priority = NotificationPriority.NORMAL
))

// 获取未读数
val unreadCount = notifySpi.getUnreadCount("user123")

// 标记已读
notifySpi.markAsRead(notifId)

// 模板通知
val templateSpi = config.messageTemplateSpi
templateSpi.create(TemplateCreateRequest(
    templateCode = "order_shipped",
    templateName = "订单发货通知",
    channel = NotificationChannel.IN_APP,
    titleTemplate = "订单 \${orderNo} 已发货",
    contentTemplate = "您的订单已发货，物流公司：\${logistics}"
))

val rendered = templateSpi.render("order_shipped", mapOf(
    "orderNo" to "ORD001",
    "logistics" to "顺丰速运"
))
```

## 自定义实现

生产环境中，应实现SPI接口替换为持久化存储：

```kotlin
// 数据库实现的RoleSpi
class DatabaseRoleService(
    private val roleRepository: RoleRepository
) : RoleSpi {
    override fun create(request: RoleCreateRequest): RoleDTO {
        // 实现持久化逻辑
    }
    // ... 其他方法
}

// 使用自定义实现
val config = systemModule {
    roleSpi = DatabaseRoleService(roleRepository)
}
```

## 注意事项

1. **内存实现仅用于开发测试**，生产环境需替换为持久化实现
2. SPI接口设计注重**最小可用性**，可根据业务需求扩展
3. 所有DTO使用**不可变数据类**，保证线程安全
4. 异常继承 `SystemException`，便于统一处理
