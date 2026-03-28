#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
cd "$ROOT_DIR"

ALL_ZERO_SHA="0000000000000000000000000000000000000000"
BASE_SHA="${BASE_SHA:-}"
HEAD_SHA="${HEAD_SHA:-HEAD}"
MANUAL_MODULES="${MANUAL_MODULES:-}"

trim() {
  local value="$1"
  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"
  printf '%s' "$value"
}

append_unique() {
  local array_name="$1"
  local value="$2"
  local existing
  local existing_values=()

  eval "existing_values=(\"\${${array_name}[@]-}\")"
  for existing in "${existing_values[@]}"; do
    if [[ "$existing" == "$value" ]]; then
      return 0
    fi
  done

  eval "${array_name}+=(\"\$value\")"
}

join_by_space() {
  local joined=""
  local item
  for item in "$@"; do
    [[ -z "$item" ]] && continue
    if [[ -n "$joined" ]]; then
      joined+=" "
    fi
    joined+="$item"
  done
  printf '%s' "$joined"
}

write_output() {
  local name="$1"
  local value="$2"
  if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
    {
      printf '%s<<__CODEX__\n' "$name"
      printf '%s\n' "$value"
      printf '__CODEX__\n'
    } >> "$GITHUB_OUTPUT"
  else
    printf '%s=%s\n' "$name" "$value"
  fi
}

normalize_to_project_path() {
  local raw="$1"
  local value
  value="$(trim "$raw")"
  value="${value#./}"
  value="${value%/}"
  [[ -z "$value" ]] && return 1

  if [[ "$value" == :* ]]; then
    printf '%s\n' "$value"
    return 0
  fi

  printf ':%s\n' "${value//\//:}"
}

find_module_dir_for_file() {
  local file="$1"
  local dir="$file"
  if [[ ! -d "$dir" ]]; then
    dir="$(dirname "$dir")"
  fi

  while [[ "$dir" != "." && "$dir" != "/" ]]; do
    if [[ -f "$dir/build.gradle.kts" ]]; then
      printf '%s\n' "$dir"
      return 0
    fi
    dir="$(dirname "$dir")"
  done

  return 1
}

declare -a candidate_paths=()

if [[ -n "$(trim "$MANUAL_MODULES")" ]]; then
  while IFS= read -r raw_module; do
    raw_module="$(trim "$raw_module")"
    [[ -z "$raw_module" ]] && continue
    project_path="$(normalize_to_project_path "$raw_module")" || continue
    append_unique candidate_paths "$project_path"
  done < <(printf '%s\n' "$MANUAL_MODULES" | awk '{ gsub(/,/, " "); for (i = 1; i <= NF; i++) print $i }')
else
  if [[ -n "$BASE_SHA" && "$BASE_SHA" != "$ALL_ZERO_SHA" ]]; then
    diff_command=(git diff --name-only "$BASE_SHA" "$HEAD_SHA")
  else
    diff_command=(git diff-tree --no-commit-id --name-only -r "$HEAD_SHA")
  fi

  while IFS= read -r changed_file; do
    [[ "$changed_file" == lib/* ]] || continue
    module_dir="$(find_module_dir_for_file "$changed_file")" || continue
    project_path="$(normalize_to_project_path "$module_dir")" || continue
    append_unique candidate_paths "$project_path"
  done < <("${diff_command[@]}")
fi

included_projects="$(
  ./gradlew -q projects |
    sed -n "s/.*[Pp]roject '\\([^']*\\)'.*/\\1/p" |
    grep '^:' |
    sort -u
)"

declare -a publishable_paths=()

declare -a publish_tasks=()

set +u
for candidate_path in "${candidate_paths[@]}"; do
  if ! grep -Fxq "$candidate_path" <<< "$included_projects"; then
    continue
  fi

  if ./gradlew -q "${candidate_path}:tasks" --group publishing | grep -q "publishToMavenCentral - Publishes to Maven Central"; then
    append_unique publishable_paths "$candidate_path"
  fi
done

for publishable_path in "${publishable_paths[@]}"; do
  publish_tasks+=("${publishable_path}:publishToMavenCentral")
done

modules_output="$(join_by_space "${publishable_paths[@]}")"
tasks_output="$(join_by_space "${publish_tasks[@]}")"
set -u

write_output "modules" "$modules_output"
write_output "tasks" "$tasks_output"
