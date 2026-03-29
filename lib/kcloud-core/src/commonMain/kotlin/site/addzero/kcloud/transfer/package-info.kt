/**
 * 传输管理层 - 传输任务的生命周期管理
 *
 * ## 职责
 * - 管理传输任务的创建、排队、执行
 * - 支持传输优先级和并发控制
 * - 传输历史记录管理
 *
 * ## 与 SyncEngine 的区别
 * - SyncEngine: 负责"检测需要做什么"
 * - TransferManager: 负责"怎么执行传输"
 *
 * ## 核心概念
 * - TransferTask: 传输任务定义
 * - TransferQueue: 传输队列管理
 * - TransferHistory: 传输历史记录
 *
 * @author zjarlin
 * @since 1.0.0
 */
package site.addzero.kcloud.transfer
