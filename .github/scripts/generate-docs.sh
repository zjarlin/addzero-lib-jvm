#!/bin/bash
set -euo pipefail

# ── Docusaurus 文档生成脚本 ──
# 收集仓库中所有 README.md，生成 Docusaurus docs 内容。

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
SITE_DIR="$REPO_DIR/docs"
CONTENT_DIR="$SITE_DIR/content"

escape_yaml() {
  printf "%s" "$1" | sed "s/'/''/g"
}

strip_first_h1() {
  awk '
    BEGIN {
      skipped = 0
    }
    {
      if (skipped == 0 && $0 ~ /^# /) {
        skipped = 1
        next
      }
      print
    }
  ' "$1"
}

rm -rf "$CONTENT_DIR"
mkdir -p "$CONTENT_DIR"

intro_links_file="$(mktemp)"
trap 'rm -f "$intro_links_file"' EXIT
: > "$intro_links_file"

cd "$REPO_DIR"

find . -name "README.md" -type f \
  ! -path "./README.md" \
  ! -path "./docs/*" \
  ! -path "*/.git/*" \
  ! -path "*/node_modules/*" \
  ! -path "*/build/*" \
  ! -path "*/target/*" \
  | sort \
  | while read -r readme; do
    rel_path="${readme#./}"
    module_dir=$(dirname "$rel_path")
    module_name=$(basename "$module_dir")
    title=$(head -n 5 "$readme" | grep -m1 '^#' | sed 's/^#*[[:space:]]*//' || true)

    if [ -z "$title" ]; then
      title="$module_name"
    fi

    target="$CONTENT_DIR/$module_dir/index.md"
    mkdir -p "$(dirname "$target")"

    {
      echo "---"
      echo "title: '$(escape_yaml "$title")'"
      echo "description: '$(escape_yaml "自动收集自 $rel_path")'"
      echo "---"
      echo ""
      echo "> 自动收集自 \`$rel_path\`。"
      echo ""
      strip_first_h1 "$readme"
      echo ""
      echo "## Maven / Gradle"
      echo ""
      echo "已发布至 Maven Central。"
      echo ""
      echo '```kotlin'
      echo "implementation(\"site.addzero:$module_name:最新版本\")"
      echo '```'
      echo ""
      echo '```xml'
      echo "<dependency>"
      echo "    <groupId>site.addzero</groupId>"
      echo "    <artifactId>$module_name</artifactId>"
      echo "    <version>最新版本</version>"
      echo "</dependency>"
      echo '```'
    } > "$target"

    echo "- [$title](/$module_dir/)" >> "$intro_links_file"
  done

{
  echo "---"
  echo "title: '小鳄鱼'"
  echo "slug: /"
  echo "---"
  echo ""
  echo "# 小鳄鱼"
  echo ""
  echo "addzero-lib-jvm 的自动化文档站点，使用 Docusaurus 构建。"
  echo ""
  echo "## 说明"
  echo ""
  echo "- 自动收集仓库内各模块的 \`README.md\`。"
  echo "- 自动补充 Maven / Gradle 依赖片段。"
  echo "- 通过 GitHub Pages 自动部署。"
  echo ""
  echo "## 模块导航"
  echo ""
  if [ -s "$intro_links_file" ]; then
    cat "$intro_links_file"
  else
    echo "暂未发现可收集的模块 README。"
  fi
} > "$CONTENT_DIR/index.md"

doc_count="$(find "$CONTENT_DIR" -name "*.md" | wc -l | tr -d ' ')"
echo "文档生成完成！共生成 $doc_count 个 Docusaurus 文档。"
