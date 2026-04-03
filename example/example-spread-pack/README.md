# Example Spread Pack

Consumer example for `site.addzero.kcp.spread-pack`.

Default mode uses `includeBuild("../../")`, so inside this repository you can run it directly:

```bash
./gradlew -p example/example-spread-pack clean test run --no-configuration-cache
```

`includeBuild("../../")` currently inherits root-build configuration that starts `git` during configuration time, so `--no-configuration-cache` is the reliable local verification path for this repo checkout.

If you want to verify the published artifacts instead of the included build:

```bash
./gradlew --no-configuration-cache \
  :lib:kcp:spread-pack:kcp-spread-pack-annotations:publishToMavenLocal \
  :lib:kcp:spread-pack:kcp-spread-pack-plugin:publishToMavenLocal \
  :lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin:publishToMavenLocal

ADDZERO_USE_INCLUDED_BUILD=false ./gradlew -p example/example-spread-pack clean test run --no-configuration-cache
```

This example covers:

- plain `@SpreadPack` carrier expansion
- `@SpreadArgsOf` selecting a definite overload from an overload set
- generated overload calls from normal Kotlin source
