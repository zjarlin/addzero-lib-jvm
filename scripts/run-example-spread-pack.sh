#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
EXAMPLE_DIR="$ROOT_DIR/example/example-spread-pack"
MODE="${1:-run}"

read_version() {
  awk -F= '$1 == "version" { print $2; exit }' "$ROOT_DIR/gradle.properties"
}

require_local_artifact() {
  local artifact_name="$1"
  local version="$2"
  local artifact_dir="$HOME/.m2/repository/site/addzero/$artifact_name/$version"
  if [[ ! -f "$artifact_dir/$artifact_name-$version.jar" ]]; then
    echo "Missing mavenLocal artifact: $artifact_name:$version" >&2
    echo "Publish these first:" >&2
    echo "  ./gradlew --configure-on-demand \\" >&2
    echo "    :lib:kcp:spread-pack:kcp-spread-pack-annotations:publishToMavenLocal \\" >&2
    echo "    :lib:kcp:spread-pack:kcp-spread-pack-plugin:publishToMavenLocal \\" >&2
    echo "    :lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin:publishToMavenLocal" >&2
    echo "If that publish path hits checkout conflict, clean local changes under checkouts/build-logic first." >&2
    exit 1
  fi
}

ensure_local_artifacts() {
  local version
  version="$(read_version)"
  require_local_artifact "kcp-spread-pack-annotations" "$version"
  require_local_artifact "kcp-spread-pack-plugin" "$version"
  require_local_artifact "kcp-spread-pack-gradle-plugin" "$version"
}

case "$MODE" in
  check)
    ensure_local_artifacts
    echo "spread-pack example prerequisites are ready"
    ;;
  test)
    ensure_local_artifacts
    exec env ADDZERO_USE_INCLUDED_BUILD=false \
      "$ROOT_DIR/gradlew" \
      -Dkotlin.compiler.execution.strategy=in-process \
      -p "$EXAMPLE_DIR" \
      clean test \
      --no-configuration-cache \
      --no-daemon
    ;;
  run)
    ensure_local_artifacts
    exec env ADDZERO_USE_INCLUDED_BUILD=false \
      "$ROOT_DIR/gradlew" \
      -Dkotlin.compiler.execution.strategy=in-process \
      -p "$EXAMPLE_DIR" \
      clean test run \
      --no-configuration-cache \
      --no-daemon
    ;;
  *)
    echo "Usage: scripts/run-example-spread-pack.sh [check|test|run]" >&2
    exit 1
    ;;
esac
