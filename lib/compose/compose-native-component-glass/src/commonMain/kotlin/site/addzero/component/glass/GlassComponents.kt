package site.addzero.component.glass

/**
 * VibePocket 玻璃组件库
 * 
 * 参考 gaze-glassy 库的设计理念，提供统一的水玻璃效果组件
 * 
 * 主要组件：
 * - GlassButton: 基础玻璃按钮
 * - NeonGlassButton: 霓虹发光按钮  
 * - LiquidGlassButton: 液体玻璃按钮
 * - GlassCard: 基础玻璃卡片
 * - NeonGlassCard: 霓虹玻璃卡片
 * - LiquidGlassCard: 液体玻璃卡片
 * - GlassSidebar: 玻璃侧边栏
 * - CompactGlassSidebar: 紧凑型侧边栏
 * - GlassTextField: 玻璃输入框
 * - GlassSearchField: 玻璃搜索框
 * - GlassTextArea: 玻璃多行输入框
 * 
 * 特殊卡片：
 * - GlassInfoCard: 信息展示卡片
 * - GlassStatCard: 统计数据卡片
 * - GlassFeatureCard: 功能介绍卡片
 * 
 * 使用方式：
 * ```kotlin
 * // 基础按钮
 * GlassButton(
 *     text = "点击我",
 *     onClick = { }
 * )
 * 
 * // 霓虹按钮
 * NeonGlassButton(
 *     text = "发光按钮",
 *     onClick = { },
 *     glowColor = GlassColors.NeonCyan
 * )
 * 
 * // 玻璃卡片
 * GlassCard {
 *     Text("卡片内容")
 * }
 * 
 * // 侧边栏
 * GlassSidebar(
 *     items = sidebarItems,
 *     onItemClick = { item -> }
 * )
 * ```
 * 
 * 颜色主题：
 * - GlassColors.NeonCyan: 霓虹青色
 * - GlassColors.NeonPurple: 霓虹紫色  
 * - GlassColors.NeonMagenta: 霓虹洋红
 * - GlassColors.NeonPink: 霓虹粉色
 * - GlassColors.DarkBackground: 深色背景
 * - GlassColors.DarkSurface: 深色表面
 */

// 所有组件都已在各自的文件中定义，这里只是文档说明
