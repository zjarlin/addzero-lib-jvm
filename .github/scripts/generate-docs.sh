#!/bin/bash

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

# 递归查找所有 README.md 文件（排除 docs 目录本身和隐藏目录）
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
    # 获取目录名作为标题
    dir_name=$(dirname "$rel_path")
    # 生成侧边栏条目
    echo "- [$dir_name](../$rel_path)" >> docs/_sidebar.md
  done

echo "文档生成完成！"
