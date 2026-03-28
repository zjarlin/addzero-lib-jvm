# Maven Central publish workflow

## Trigger rules

- `push` to `main`: only publishes when an included module under `lib/**` changed and `gradle.properties` version changed in the same push range.
- `workflow_dispatch`: can publish one or more modules manually without requiring a version bump in that run.

## Manual input format

The `modules` input accepts either Gradle project paths or repository directories:

- `:lib:kcp:kcp-i18n-runtime`
- `lib/kcp/kcp-i18n-runtime`
- Multiple values can be separated by comma, whitespace, or newline.

## Required GitHub secrets

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `MAVEN_SIGNING_KEY`
- `MAVEN_SIGNING_PASSWORD`
- `MAVEN_SIGNING_KEY_ID` (optional)

## Notes

- Maven Central release versions are immutable. If you want auto publish on `push`, you must bump `gradle.properties` `version=...` before pushing.
- `workflow_dispatch` is useful for retrying a failed publish or manually releasing a new version. It still cannot overwrite an already published version on Maven Central.
- The workflow installs JDK 8 and JDK 17 because this repository disables Gradle toolchain auto-download.
- Publishing uses `publishToMavenCentral` on the changed modules, and Gradle deduplicates shared task dependencies in a single invocation.
