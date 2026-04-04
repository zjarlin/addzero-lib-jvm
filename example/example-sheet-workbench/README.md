# Example Sheet Workbench

Standalone runnable example for `site.addzero:compose-sheet-spi` and `site.addzero:compose-native-component-sheet`.

Inside this repository, the simplest entry is the repo-level launcher script:

```bash
./scripts/run-example-sheet-workbench.sh engine
./scripts/run-example-sheet-workbench.sh ui
./scripts/run-example-sheet-workbench.sh ui-keep
```

- `engine`: runs the pure sheet engine scenario and prints checkpoints
- `ui`: opens the standalone desktop workbench and auto exits after 1500 ms by default
- `ui-keep`: opens the standalone desktop workbench and keeps the window open

You can override the auto-exit timeout for `ui` with:

```bash
SHEET_PREVIEW_AUTO_EXIT_MILLIS=3000 ./scripts/run-example-sheet-workbench.sh ui
```

If you prefer raw Gradle commands, use:

```bash
./gradlew -p example/example-sheet-workbench runEngineScenario --no-configuration-cache
./gradlew -p example/example-sheet-workbench previewSheetWorkbench -Dsheet.preview.autoExitMillis=1500 --no-configuration-cache
```

`includeBuild("../../")` currently inherits root-build configuration that starts `git` during configuration time, so `--no-configuration-cache` is the reliable local verification path for this repo checkout.

If the root included build is temporarily broken, you can publish the two sheet modules to `mavenLocal` and run this example without `includeBuild`:

```bash
./gradlew \
  :lib:compose:compose-sheet-spi:publishToMavenLocal \
  :lib:compose:compose-native-component-sheet:publishToMavenLocal \
  --no-daemon

ADDZERO_USE_INCLUDED_BUILD=false ./scripts/run-example-sheet-workbench.sh engine
ADDZERO_USE_INCLUDED_BUILD=false ./scripts/run-example-sheet-workbench.sh ui
```

This example covers:

- pure engine scenario execution without UI
- standalone desktop workbench startup
- clipboard-like TSV copy/paste over the sheet engine
- in-memory sheet data source for local experimentation
