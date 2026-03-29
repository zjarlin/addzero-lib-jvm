/**
 * 状态管理层 - 应用全局状态中心
 *
 * ## 职责
 * - 集中管理应用的所有 UI 状态
 * - 提供响应式状态流 (StateFlow)
 * - 同步状态与托盘图标状态映射
 *
 * ## 核心组件
 * - [AppState] - 不可变状态数据类
 * - [AppStateManager] - 状态管理器单例
 * - [SyncStatus] - 同步状态枚举
 * - [TrayIconState] - 托盘图标状态
 *
 * ## 状态流向
 * ```
 * 同步引擎/用户操作 -> AppStateManager.updateXxx()
 *                              |
 *                              v
 *                    MutableStateFlow<AppState>
 *                              |
 *                              v
 *                     UI 组件订阅并自动更新
 * ```
 *
 * ## 使用示例
 * ```kotlin
 * // Compose 中订阅状态
 * val state by AppStateManager.state.collectAsState()
 *
 * // 更新状态
 * AppStateManager.updateSyncStatus(SyncStatus.SYNCING, "正在上传...")
 * AppStateManager.updateProgress(0.5f, "document.pdf")
 * ```
 *
 * @author zjarlin
 * @since 1.0.0
 */
package site.addzero.kcloud.state
