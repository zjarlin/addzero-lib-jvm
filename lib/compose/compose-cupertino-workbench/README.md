# compose-cupertino-workbench

- Module path: `lib/compose/compose-cupertino-workbench`
- Maven coordinate: `site.addzero:compose-cupertino-workbench`
- Purpose: reusable Cupertino-flavored workbench shell, theme, buttons, metrics, and compatibility facade APIs for `kcloud`

## Usage

```kotlin
CupertinoWorkbenchTheme {
  RenderCupertinoWorkbenchScaffolding(scaffolding = scaffolding)
}
```

## Notes

- Built for Compose Multiplatform common UI code
- Keeps `ScaffoldingSpi`, route wiring, and Koin integration unchanged on the app side
