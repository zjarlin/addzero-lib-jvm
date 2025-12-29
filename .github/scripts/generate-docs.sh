#!/bin/bash

# 清理旧的文档文件（保留 index.html）
find docs -type f -name "*.md" -delete
find docs -type d -empty -delete

# 创建 index.html
cat > docs/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>addzero-lib-jvm Documentation</title>
  <link rel="stylesheet" href="//cdn.jsdelivr.net/npm/docsify@4/lib/themes/vue.css">
</head>
<body>
  <div id="app"></div>
  <script>
    window.$docsify = {
      name: 'addzero-lib-jvm',
      repo: 'https://github.com/zjarlin/addzero-lib-jvm',
      loadSidebar: true,
      subMaxLevel: 3,
      auto2top: true,
      search: 'auto',
      relativePath: true,
      alias: {
        '/.*/_sidebar.md': '/_sidebar.md'
      }
    }
  </script>
  <script src="//cdn.jsdelivr.net/npm/docsify@4"></script>
  <script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/search.min.js"></script>
</body>
</html>
EOF

# 生成文档首页
cat > docs/README.md << 'EOF'
# addzero-lib-jvm

欢迎来到 addzero-lib-jvm 文档站点。

## 项目简介

这是一个 Kotlin/JVM 多模块项目，包含多个工具库和处理器。

## 快速导航

使用左侧侧边栏浏览各个模块的文档。
EOF

# 生成侧边栏
echo "- [首页](/)" > docs/_sidebar.md
echo "" >> docs/_sidebar.md

# 递归查找所有 README.md 文件并复制到 docs 目录
find . -name "README.md" -type f \
  ! -path "./docs/*" \
  ! -path "*/node_modules/*" \
  ! -path "*/.git/*" \
  ! -path "*/build/*" \
  ! -path "*/target/*" \
  | sort \
  | while read -r readme; do
    # 获取相对路径
    rel_path="${readme#./}"
    # 目标路径
    target="docs/$rel_path"
    # 创建目标目录
    mkdir -p "$(dirname "$target")"
    # 复制文件
    cp "$readme" "$target"

    # 读取第一行标题
    title=$(head -n 1 "$readme" | sed 's/^#\+\s*//')

    # 如果没有标题，使用目录名
    if [ -z "$title" ]; then
      title=$(dirname "$rel_path")
    fi

    # 生成侧边栏条目
    echo "- [$title](/$rel_path)" >> docs/_sidebar.md
  done

echo "文档生成完成！"
