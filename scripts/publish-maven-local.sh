#!/bin/sh

set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$root"

ADDZERO_SIGN_ARTIFACTS=false kotlinc -script scripts/generate-toolchain-jvm-modules.main.kts

# The JVM and KMP projects reference each other through Maven coordinates because
# Kotlin Toolchain project dependencies cannot cross project.yaml boundaries.
./kotlin publish mavenLocal \
  --project-dir toolchain/kmp-project \
  -m tool-str,tool-jdbc-model,kcp-spread-pack-annotations
./kotlin publish mavenLocal \
  -m jimmer-entity-spi,kcp-spread-pack-plugin,lsi-ksp,tool-io-codegen,tool-jdbc

scope=all
case "${1-}" in
  --jvm)
    scope=jvm
    shift
    ;;
  --kmp)
    scope=kmp
    shift
    ;;
esac

# Preserve the old targeted form: `publish-maven-local.sh -m module` targets JVM.
if [ "$scope" = all ] && [ "$#" -gt 0 ]; then
  scope=jvm
fi

if [ "$scope" = kmp ]; then
  ./kotlin publish mavenLocal --project-dir toolchain/kmp-project "$@"
else
  # A targeted JVM module may depend on any migrated KMP artifact.
  ./kotlin publish mavenLocal --project-dir toolchain/kmp-project
fi

if [ "$scope" = all ] || [ "$scope" = jvm ]; then
  ./kotlin publish mavenLocal "$@"
fi
