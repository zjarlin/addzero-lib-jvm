#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODE="${1:-ui}"
AUTO_EXIT_MILLIS="${SHEET_PREVIEW_AUTO_EXIT_MILLIS:-1500}"

case "$MODE" in
  engine)
    exec "$ROOT_DIR/gradlew" \
      -Dkotlin.compiler.execution.strategy=in-process \
      -p "$ROOT_DIR/example/example-sheet-workbench" \
      runEngineScenario \
      --no-configuration-cache \
      --no-daemon
    ;;
  ui)
    exec "$ROOT_DIR/gradlew" \
      -Dkotlin.compiler.execution.strategy=in-process \
      -p "$ROOT_DIR/example/example-sheet-workbench" \
      previewSheetWorkbench \
      "-Dsheet.preview.autoExitMillis=$AUTO_EXIT_MILLIS" \
      --no-configuration-cache \
      --no-daemon
    ;;
  ui-keep)
    exec "$ROOT_DIR/gradlew" \
      -Dkotlin.compiler.execution.strategy=in-process \
      -p "$ROOT_DIR/example/example-sheet-workbench" \
      previewSheetWorkbench \
      --no-configuration-cache \
      --no-daemon
    ;;
  *)
    echo "Usage: scripts/run-example-sheet-workbench.sh [engine|ui|ui-keep]" >&2
    exit 1
    ;;
esac
