#!/bin/bash
set -euo pipefail

# ── mdBook 文档生成脚本 ──
# 收集仓库中所有 README.md，生成 SUMMARY.md，供 mdBook 构建。

SRC_DIR="docs/src"

rm -rf "$SRC_DIR"
mkdir -p "$SRC_DIR"

# ── 首页 ──
if [ -f README.md ]; then
  cp README.md "$SRC_DIR/README.md"
else
  cat > "$SRC_DIR/README.md" << 'EOF'
# addzero-lib-jvm

Kotlin/JVM 多模块工具库集合。
EOF
fi

# ── 收集子模块 README ──
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
    target="$SRC_DIR/$rel_path"
    mkdir -p "$(dirname "$target")"

    module_dir=$(dirname "$rel_path")
    module_name=$(basename "$module_dir")

    {
      cat "$readme"
      echo ""
      echo "---"
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
  done

# ── 生成 SUMMARY.md ──
SUMMARY="$SRC_DIR/SUMMARY.md"
echo "# Summary" > "$SUMMARY"
echo "" >> "$SUMMARY"
echo "[首页](README.md)" >> "$SUMMARY"
echo "" >> "$SUMMARY"

find "$SRC_DIR" -name "README.md" -type f \
  ! -path "$SRC_DIR/README.md" \
  | sort \
  | while read -r mdfile; do
    rel="${mdfile#$SRC_DIR/}"
    module_dir=$(dirname "$rel")
    module_name=$(basename "$module_dir")

    title=$(head -n 5 "$mdfile" | grep -m1 '^#' | sed 's/^#*[[:space:]]*//' || true)
    if [ -z "$title" ]; then
      title="$module_name"
    fi

    echo "- [$title]($rel)" >> "$SUMMARY"
  done

echo ""
echo "文档生成完成！共处理 $(find "$SRC_DIR" -name "README.md" | wc -l | tr -d ' ') 个文档。"