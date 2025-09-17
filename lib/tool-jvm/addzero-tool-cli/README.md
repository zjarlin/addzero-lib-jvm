# dotfiles-cli
这是一个用Kotlin编写的CLI工具，用于配置新的操作系统。它支持Windows、macOS和Linux。
## 功能特性
- 跨平台支持（Windows、macOS、Linux）
- 包管理支持：Windows使用winget，macOS使用brew，Linux可配置包管理器
- Dotfiles从Git仓库同步
- 环境变量配置
- 任务跟踪与本地缓存
- 基于注解的任务执行（注解使用中文描述）
- 使用KOIN依赖注入框架管理组件依赖
- 符合XDG Base Directory规范（使用`~/.config/dotfiles-cli`作为配置和缓存目录）
- 完整的中英文双语支持
- 交互式任务执行界面
- GraalVM Native Image支持（无需安装JDK即可运行）

## 安装

### 类Unix系统（macOS、Linux）

```bash
# 克隆仓库
git clone <repository-url>
cd dotfiles-cli

# 运行安装脚本
./install.sh
```

### Windows系统

```cmd
REM 克隆仓库
git clone <repository-url>
cd dotfiles-cli

REM 运行安装脚本
install.bat
```

## 使用方法

安装完成后，可以使用以下命令：

```bash
# 初始化系统配置
dotfiles init

# 安装软件包
dotfiles install

# 同步dotfiles
dotfiles sync

# 显示帮助信息
dotfiles help

# 使用英文界面
dotfiles help --lang=en

# 使用中文界面（默认）
dotfiles help --lang=zh
```

### 交互式模式

不带参数运行CLI工具将进入交互式模式，所有任务都会自动编号，用户可以通过输入数字来执行特定任务：

```bash
# 进入交互式模式
dotfiles
```

在交互式模式下，系统会显示所有可用任务的编号和描述，用户可以输入任务编号来执行任务，或输入'quit'或'exit'退出。

## 构建

### 构建JAR文件

```bash
# 构建可执行JAR
./gradlew build

# 运行JAR文件
java -jar app/build/libs/app.jar
```

### 构建Native Image（GraalVM）

要构建Native Image，您需要先安装GraalVM：

#### macOS

```bash
# 使用Homebrew安装GraalVM
brew install --cask graalvm/tap/graalvm-ce-java17

# 安装native-image工具
gu install native-image
```

#### Linux

```bash
# 下载GraalVM
wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.0/graalvm-ce-java17-linux-amd64-22.3.0.tar.gz

# 解压
tar -xzf graalvm-ce-java17-linux-amd64-22.3.0.tar.gz

# 设置环境变量
export GRAALVM_HOME=/path/to/graalvm-ce-java17-22.3.0
export PATH=$GRAALVM_HOME/bin:$PATH

# 安装native-image工具
gu install native-image
```

#### Windows

```cmd
REM 下载GraalVM
REM 从 https://github.com/graalvm/graalvm-ce-builds/releases 下载Windows版本

REM 解压到目录

REM 设置环境变量
set GRAALVM_HOME=C:\path\to\graalvm-ce-java17-22.3.0
set PATH=%GRAALVM_HOME%\bin;%PATH%

REM 安装native-image工具
gu install native-image
```

#### 构建Native Image

```bash
# 构建Native Image（需要安装GraalVM）
./gradlew nativeCompile

# 运行Native Image
./app/build/native/nativeCompile/dotfiles
```

## 国际化支持

本工具支持中英文双语界面。可以通过`--lang=en`或`--lang=zh`参数来切换语言。
语言配置文件位于`~/.config/dotfiles-cli/i18n-config.json`，采用分层JSON结构，用户可以自定义翻译文本。

### i18n-config.json结构

国际化配置文件采用分层结构，按功能模块组织：

```json
{
  "zh": {
    "app": {
      "title": "Dotfiles CLI 工具",
      "running_on": "运行在"
    },
    "commands": {
      "usage": "用法: dotfiles [命令] [选项]",
      "available": "命令:"
    }
    // ... 更多模块
  },
  "en": {
    "app": {
      "title": "Dotfiles CLI Tool",
      "running_on": "Running on"
    },
    "commands": {
      "usage": "Usage: dotfiles [command] [options]",
      "available": "Commands:"
    }
    // ... 更多模块
  }
}
```

要获取特定消息，使用`LanguageManager.getMessage("section", "key", args...)`方法。

## 配置

工具使用`~/.config/dotfiles-cli/config.json`作为配置文件。创建此文件来自定义行为：

```json
{
  "repositoryUrl": "https://github.com/yourusername/dotfiles.git",
  "dotfiles": [
    ".bashrc",
    ".vimrc",
    ".gitconfig"
  ],
  "packages": [
    "git",
    "vim",
    "curl"
  ],
  "environmentVariables": {
    "EDITOR": "vim"
  },
  "packageManager": {
    "packageManager": "apt",
    "packageManagerPrefix": "sudo apt install -y"
  }
}
```

## 开发

本项目使用[Gradle](https://gradle.org/)构建。
可以通过点击右侧工具栏的Gradle图标使用Gradle工具窗口，或直接在终端中运行：

* 运行 `./gradlew run` 构建并运行应用程序
* 运行 `./gradlew build` 仅构建应用程序
* 运行 `./gradlew check` 运行所有检查，包括测试
* 运行 `./gradlew clean` 清理所有构建输出

注意使用Gradle Wrapper（`./gradlew`）。
这是在生产项目中使用Gradle的推荐方式。

[了解更多关于Gradle Wrapper的信息](https://docs.gradle.org/current/userguide/gradle_wrapper.html)。

[了解更多关于Gradle任务的信息](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks)。

本项目采用建议的多模块设置，由`app`和`utils`子项目组成。
共享的构建逻辑被提取到位于`buildSrc`中的约定插件中。

本项目使用版本目录（参见`gradle/libs.versions.toml`）来声明和管理依赖版本，
并使用构建缓存和配置缓存（参见`gradle.properties`）。

### 依赖注入

本项目使用[KOIN](https://insert-koin.io/)作为依赖注入框架，以更好地管理组件之间的依赖关系。
KOIN模块定义在`utils/src/main/kotlin/site/addzero/cli/di/`目录中。
