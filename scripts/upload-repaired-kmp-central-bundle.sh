#!/bin/sh

set -eu

module=${1-}
if [ -z "$module" ]; then
  printf 'Usage: %s <module>\n' "$0" >&2
  exit 2
fi

require_env() {
  if [ -z "$2" ]; then
    printf 'Missing required environment variable: %s\n' "$1" >&2
    exit 2
  fi
}

require_env KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME "${KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME-}"
require_env KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD "${KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD-}"
require_env KOTLIN_TOOLCHAIN_SIGNING_KEY "${KOTLIN_TOOLCHAIN_SIGNING_KEY-}"

root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$root"

./kotlin package \
  --project-dir toolchain/kmp-project \
  -m "$module" \
  -f maven-central-bundle

bundle="toolchain/kmp-project/build/tasks/_${module}_prepareMavenCentralBundle/${module}-central-bundle.zip"
if [ ! -f "$bundle" ]; then
  printf 'Maven Central bundle was not generated: %s\n' "$bundle" >&2
  exit 1
fi
version=$(
  awk '
    $1 == "version:" {
      print $2
      exit
    }
  ' "toolchain/kmp-project/modules/$module/module.yaml"
)

work=$(mktemp -d)
signing_home="$work/gpg"
contents="$work/contents"
repaired_bundle="$work/${module}-central-bundle.zip"
cleanup() {
  if [ -d "${work-}" ]; then
    find "$work" -depth -delete
  fi
}
trap cleanup EXIT HUP INT TERM
mkdir -m 700 "$signing_home"
mkdir "$contents"
unzip -q "$bundle" -d "$contents"

printf '%s\n' "$KOTLIN_TOOLCHAIN_SIGNING_KEY" |
  gpg --homedir "$signing_home" --batch --import >/dev/null 2>&1

find "$contents" -type f \( -name '*.asc' -o -name '*.md5' -o -name '*.sha1' \) -delete
find "$contents" -type f -print | while IFS= read -r artifact; do
  gpg \
    --homedir "$signing_home" \
    --batch \
    --yes \
    --pinentry-mode loopback \
    --passphrase "${KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE-}" \
    --armor \
    --detach-sign \
    --output "$artifact.asc" \
    "$artifact"
  md5 -q "$artifact" > "$artifact.md5"
  shasum -a 1 "$artifact" | awk '{print $1}' > "$artifact.sha1"
done

find "$contents" -type f ! \( -name '*.asc' -o -name '*.md5' -o -name '*.sha1' \) -print |
while IFS= read -r artifact; do
  test "$(md5 -q "$artifact")" = "$(cat "$artifact.md5")"
  test "$(shasum -a 1 "$artifact" | awk '{print $1}')" = "$(cat "$artifact.sha1")"
  gpg --homedir "$signing_home" --batch --verify "$artifact.asc" "$artifact" >/dev/null 2>&1
done

(
  cd "$contents"
  find . -type f -print | LC_ALL=C sort | zip -q "$repaired_bundle" -@
)

auth=$(
  printf '%s:%s' \
    "$KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME" \
    "$KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD" |
    base64
)
curl_config() {
  printf 'header = "Authorization: Bearer %s"\n' "$auth"
  printf 'silent\nshow-error\nfail\n'
}

deployment=$(
  curl_config |
    curl \
      --config - \
      --form "bundle=@$repaired_bundle;type=application/octet-stream" \
      "https://central.sonatype.com/api/v1/publisher/upload?name=${module}-${version}&publishingType=AUTOMATIC"
)
printf 'Uploaded repaired Maven Central bundle for %s as deployment %s\n' "$module" "$deployment"

previous_state=
attempt=0
while [ "$attempt" -lt 300 ]; do
  response=$(
    {
      curl_config
      printf 'request = "POST"\n'
    } | curl --config - "https://central.sonatype.com/api/v1/publisher/status?id=$deployment"
  )
  state=$(printf '%s' "$response" | jq -r '.deploymentState')
  if [ "$state" != "$previous_state" ]; then
    printf 'Deployment %s: %s\n' "$deployment" "$state"
    previous_state=$state
  fi
  case "$state" in
    PUBLISHING|PUBLISHED)
      exit 0
      ;;
    FAILED)
      printf '%s\n' "$response" | jq '{deploymentId, deploymentState, errors}' >&2
      exit 1
      ;;
  esac
  attempt=$((attempt + 1))
  sleep 2
done

printf 'Timed out waiting for deployment %s\n' "$deployment" >&2
exit 1
