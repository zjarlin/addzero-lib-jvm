# dotfiles-cli
这是一个用Kotlin编写的CLI工具，用于配置新的操作系统。它支持Windows、macOS和Linux。

## 功能特性
- 跨平台支持（Windows、macOS、Linux）
- 包管理支持：Windows使用winget，macOS使用brew，Linux可配置包管理器
- Dotfiles从Git仓库同步
- 环境变量配置
- 符合XDG Base Directory规范（使用`~/.config/dotfiles-cli`作为配置和缓存目录）
- 交互式任务执行界面
- 幂等操作（任务状态持久化）

## 安装

### 从源码构建
```bash
# 克隆仓库
git clone <repository-url>
cd dotfiles-cli

# 构建项目
./gradlew build

# 运行CLI工具
./gradlew run --args="help"
```

### 使用预构建二进制文件
```bash
# 下载适用于您操作系统的二进制文件
# 解压并将其添加到PATH中
dotfiles-cli help
```

## 使用方法

### 命令行选项
```bash
# 显示帮助信息
dotfiles-cli help

# 初始化系统（安装包管理器，设置环境变量）
dotfiles-cli init

# 安装软件包
dotfiles-cli install

# 同步Dotfiles
dotfiles-cli sync

# 交互式模式
dotfiles-cli
```

### 配置文件
配置文件位于 `~/.config/dotfiles-cli/config.json`，示例配置：
```json
{
  "repositoryUrl": "https://github.com/yourusername/dotfiles.git",
  "packages": [
    "git",
    "vim",
    "curl",
    "wget"
  ],
  "dotfiles": [
    ".bashrc",
    ".vimrc",
    ".gitconfig"
  ],
  "packageManager": {
    "command": "",
    "installArgs": "install",
    "syncArgs": "sync",
    "updateArgs": "update"
  },
  "environmentVariables": {
    "EDITOR": "vim",
    "SHELL": "/bin/bash"
  }
}
```

## 设计模式

### 操作系统抽象
使用策略模式抽象Windows、macOS和Linux三大操作系统，每个操作系统都有自己的具体实现。

### 任务执行
使用命令模式和模板方法模式来执行各种配置任务，确保操作的幂等性。

### 配置管理
使用单例模式管理配置，确保配置的一致性和可访问性。

## 支持的操作系统

- **Windows**: 使用winget作为包管理器
- **macOS**: 使用Homebrew作为包管理器
- **Linux**: 支持多种包管理器（apt、yum、pacman等），可通过配置文件指定

## 开发

### 构建项目
```bash
# 在项目根目录运行
./gradlew :lib:tool-jvm:addzero-tool-cli:build
```

### 运行CLI工具
```bash
# 直接运行
./gradlew :lib:tool-jvm:addzero-tool-cli:run --args="help"

# 或者使用生成的脚本（需要先构建）
./scripts/dotfiles-cli.sh help
```

## 贡献
欢迎提交Issue和Pull Request来改进这个工具！
