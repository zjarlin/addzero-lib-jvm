#!/bin/sh

set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$root"

gradle_properties=${GRADLE_USER_HOME:-"$HOME/.gradle"}/gradle.properties

gradle_property() {
  property_name=$1
  if [ ! -f "$gradle_properties" ]; then
    return
  fi
  awk -F= -v property_name="$property_name" '
    $1 == property_name {
      sub(/^[^=]*=/, "")
      print
      exit
    }
  ' "$gradle_properties"
}

load_gradle_credentials() {
  if [ -z "${KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME-}" ]; then
    KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME=$(gradle_property mavenCentralUsername)
  fi
  if [ -z "${KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD-}" ]; then
    KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD=$(gradle_property mavenCentralPassword)
  fi
  if [ -z "${KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE-}" ]; then
    KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE=$(gradle_property signing.password)
  fi
  if [ -n "${KOTLIN_TOOLCHAIN_SIGNING_KEY-}" ]; then
    return
  fi

  signing_key_id=$(gradle_property signing.keyId)
  signing_key_ring=$(gradle_property signing.secretKeyRingFile)
  case "$signing_key_ring" in
    '~/'*) signing_key_ring="$HOME/${signing_key_ring#\~/}" ;;
  esac
  if [ -z "$signing_key_id" ] || [ ! -f "$signing_key_ring" ]; then
    return
  fi
  if ! command -v gpg >/dev/null 2>&1; then
    printf 'gpg is required to convert signing.secretKeyRingFile for Kotlin Toolchain\n' >&2
    exit 2
  fi

  signing_home=$(mktemp -d)
  chmod 700 "$signing_home"
  cleanup_signing_home() {
    if [ -d "${signing_home-}" ]; then
      find "$signing_home" -depth -delete
    fi
  }
  trap cleanup_signing_home EXIT HUP INT TERM
  gpg --homedir "$signing_home" --batch --import "$signing_key_ring" >/dev/null 2>&1
  KOTLIN_TOOLCHAIN_SIGNING_KEY=$(
    gpg \
      --homedir "$signing_home" \
      --batch \
      --pinentry-mode loopback \
      --passphrase "$KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE" \
      --armor \
      --export-secret-keys "$signing_key_id"
  )
  cleanup_signing_home
  trap - EXIT HUP INT TERM
}

require_env() {
  if [ -z "$2" ]; then
    printf 'Missing required environment variable: %s\n' "$1" >&2
    exit 2
  fi
}

load_gradle_credentials
export KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME
export KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD
export KOTLIN_TOOLCHAIN_SIGNING_KEY
export KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE

require_env KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME "${KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME-}"
require_env KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD "${KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD-}"
require_env KOTLIN_TOOLCHAIN_SIGNING_KEY "${KOTLIN_TOOLCHAIN_SIGNING_KEY-}"

if [ "${1-}" = "--check-credentials" ]; then
  printf 'Maven Central credentials and signing key are available\n'
  exit 0
fi

restore_local_config() {
  ADDZERO_SIGN_ARTIFACTS=false kotlinc -script scripts/generate-toolchain-jvm-modules.main.kts >/dev/null
}

trap restore_local_config EXIT HUP INT TERM
# Central publication starts only after both Toolchain projects pass Maven Local.
./scripts/publish-maven-local.sh

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
if [ "$scope" = all ] && [ "$#" -gt 0 ]; then
  scope=jvm
fi

ADDZERO_SIGN_ARTIFACTS=true kotlinc -script scripts/generate-toolchain-jvm-modules.main.kts
if [ "$scope" = kmp ]; then
  if [ "$#" -eq 0 ]; then
    kmp_modules=$(paste -sd, toolchain/kmp-publishing-modules.txt)
  elif [ "$#" -eq 2 ] && [ "$1" = -m ]; then
    kmp_modules=$2
  else
    printf 'KMP Central publication accepts no arguments or: --kmp -m module[,module...]\n' >&2
    exit 2
  fi
elif [ "$scope" = all ]; then
  kmp_modules=$(paste -sd, toolchain/kmp-publishing-modules.txt)
else
  kmp_modules=
fi

if [ -n "$kmp_modules" ]; then
  old_ifs=$IFS
  IFS=,
  for module in $kmp_modules; do
    ./scripts/upload-repaired-kmp-central-bundle.sh "$module"
  done
  IFS=$old_ifs
fi
if [ "$scope" = all ] || [ "$scope" = jvm ]; then
  ./kotlin publish mavenCentral "$@"
fi
