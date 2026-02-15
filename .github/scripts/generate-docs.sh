#!/bin/bash

# 清理旧的文档文件
rm -rf docs/lib
rm -f docs/README.md docs/_sidebar.md

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
      homepage: 'README.md',
      // 自动检测基础路径，解决 GitHub Pages 子路径 404 问题
      basePath: window.location.pathname.indexOf('/addzero-lib-jvm') !== -1
        ? '/addzero-lib-jvm/' 
        : '/'
    }
  </script>
  <script src="//cdn.jsdelivr.net/npm/docsify@4"></script>
  <script src="//cdn.jsdelivr.net/npm/docsify/lib/plugins/search.min.js"></script>
</body>
</html>
EOF

# 禁用 Jekyll
touch docs/.nojekyll

# 生成文档首页
cat > docs/README.md << 'EOF'
# addzero-lib-jvm

欢迎来到 addzero-lib-jvm 文档站点。

这是一个 Kotlin/JVM 多模块项目，包含多个工具库和处理器。

## 快速导航

请通过左侧侧边栏查看各个模块的详细文档和用法说明。
EOF

# 生成侧边栏
echo "- [首页](README.md)" > docs/_sidebar.md
echo "" >> docs/_sidebar.md

# 递归查找所有 README.md 文件并处理
find . -name "README.md" -type f \
  ! -path "./docs/*" \
  ! -path "./README.md" \
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
    
    # 获取模块目录名作为 artifactId
    module_dir=$(dirname "$rel_path")
    module_name=$(basename "$module_dir")
    
    # 复制内容并追加用法说明
    {
      cat "$readme"
      echo -e "\n\n---"
      echo "## 如何使用 (Maven/Gradle)"
      echo ""
      echo "此模块已发布至 Maven Central。"
      echo ""
      echo "### Gradle (Kotlin)"
      echo "\`\`\`kotlin"
      echo "implementation(\"site.addzero:$module_name:最新版本\")"
      echo "\`\`\`"
      echo ""
      echo "### Maven"
      echo "\`\`\`xml"
      echo "<dependency>"
      echo "    <groupId>site.addzero</groupId>"
      echo "    <artifactId>$module_name</artifactId>"
      echo "    <version>最新版本</version>"
      echo "</dependency>"
      echo "\`\`\`"
    } > "$target"

    # 读取第一行标题
    title=$(head -n 1 "$readme" | sed 's/^#\+\s*//')

    # 如果没有标题，使用模块名
    if [ -z "$title" ]; then
      title="$module_name"
    fi

    # 生成侧边栏条目
    echo "- [$title]($rel_path)" >> docs/_sidebar.md
  done

echo "文档生成完成！"