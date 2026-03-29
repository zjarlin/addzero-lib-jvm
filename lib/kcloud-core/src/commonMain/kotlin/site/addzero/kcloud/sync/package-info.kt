/**
 * 同步引擎层 - 核心同步逻辑
 *
 * ## 职责
 * - 检测本地和远程的文件变更
 * - 生成同步计划 ([SyncPlan])
 * - 执行上传/下载/删除操作
 * - 处理冲突检测与解决
 *
 * ## 架构设计
 * ```
 * ┌─────────────────────────────────────┐
 * │          SyncEngine                 │
 * ├─────────────────────────────────────┤
 * │  detectLocalChanges()               │
 * │  detectRemoteChanges()              │
 * │  buildSyncPlan()        ──────┐     │
 * │  executeSyncPlan()            │     │
 * │  handleConflicts()            │     │
 * └───────────────────────────────┼─────┘
 *                                 │
 *                     ┌───────────▼──────────┐
 *                     │   StorageClient      │
 *                     │   (抽象接口)          │
 *                     └──────────────────────┘
 * ```
 *
 * ## 同步算法
 * 1. 扫描本地文件系统，与数据库对比 → 本地变更
 * 2. 获取远程文件列表，与数据库对比 → 远程变更
 * 3. 合并双方变更，检测冲突
 * 4. 生成并执行同步计划
 *
 * ## 文件过滤
 * - [SyncFilter] - 同步过滤器，支持.gitignore风格规则
 * - [SyncPattern] - 单个过滤规则
 * - [SyncRules] - 规则配置（全局+本地）
 *
 * ## 状态机
 * IDLE → SCANNING → (UPLOADING/DOWNLOADING) → IDLE
 *                    ↓
 *               PAUSED/ERROR
 *
 * @author zjarlin
 * @since 1.0.0
 */
package site.addzero.kcloud.sync
