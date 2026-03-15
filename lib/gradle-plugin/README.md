# Gradle 插件相关模块

这一组放 Gradle 构建侧能力，包含 Settings 插件、Project 插件、Convention 插件和一些工具模块。

## 推荐入口

- [`settings-plugin/gradle-modules-buddy`](./settings-plugin/gradle-modules-buddy/)：模块扫描与项目装配
- [`settings-plugin/gradle-git-dependency`](./settings-plugin/gradle-git-dependency/)：Git 依赖方向
- [`project-plugin/gradle-ksp-buddy`](./project-plugin/gradle-ksp-buddy/)：KSP 构建辅助
- [`project-plugin/gradle-apt-buddy`](./project-plugin/gradle-apt-buddy/)：APT 构建辅助
- [`project-plugin/gradle-publish-budy`](./project-plugin/gradle-publish-budy/)：发布流程相关
- [`project-plugin/gradle-version-budy`](./project-plugin/gradle-version-budy/)：版本处理相关
- [`conventions/jvm-conventions/lombok-convention`](./conventions/jvm-conventions/lombok-convention/)：JVM Convention 示例
- [`conventions/jvm-conventions/spring-convention`](./conventions/jvm-conventions/spring-convention/)：Spring Convention 示例

## 分组理解

- `settings-plugin/`：更偏工程装配、模块发现、仓库级行为
- `project-plugin/`：更偏单模块构建行为
- `conventions/`：更偏统一规范和复用脚本
- `tool/` 与 `gradle-tool/`：更偏支撑插件实现的底层工具

## 适合什么时候看

- 你在找这个仓库自己的 Gradle 组织方式
- 你准备抽某一段构建逻辑复用到别的项目
