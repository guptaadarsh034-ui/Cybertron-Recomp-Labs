#!/usr/bin/env bash
# Prepare the exact ReXGlue SDK source revision used by this project.
# This script does not apply Android patches or build the SDK.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_DIR="${ROOT}/thirdparty/rexglue-sdk"
SDK_URL="https://github.com/rexglue/rexglue-sdk.git"
SDK_TAG="v0.10.0"

command -v git >/dev/null 2>&1 || { echo "error: git is required" >&2; exit 1; }

if [[ -e "${SDK_DIR}" ]]; then
  if [[ ! -d "${SDK_DIR}/.git" ]]; then
    echo "error: ${SDK_DIR} exists but is not a Git checkout; refusing to overwrite it" >&2
    exit 1
  fi

  actual_remote="$(git -C "${SDK_DIR}" remote get-url origin)"
  if [[ "${actual_remote}" != "${SDK_URL}" ]]; then
    echo "error: unexpected SDK origin: ${actual_remote}" >&2
    exit 1
  fi

  actual_tag="$(git -C "${SDK_DIR}" describe --tags --exact-match HEAD 2>/dev/null || true)"
  if [[ "${actual_tag}" != "${SDK_TAG}" ]]; then
    echo "error: SDK checkout is not exactly ${SDK_TAG} (found '${actual_tag:-untagged}'); refusing to reset it" >&2
    exit 1
  fi

  if [[ -n "$(git -C "${SDK_DIR}" status --porcelain)" ]]; then
    echo "error: SDK checkout has local changes; refusing to modify it" >&2
    exit 1
  fi
else
  mkdir -p "$(dirname "${SDK_DIR}")"
  git clone --depth 1 --branch "${SDK_TAG}" "${SDK_URL}" "${SDK_DIR}"
fi

git -C "${SDK_DIR}" submodule update --init --recursive --depth 1

actual_tag="$(git -C "${SDK_DIR}" describe --tags --exact-match HEAD 2>/dev/null || true)"
if [[ "${actual_tag}" != "${SDK_TAG}" ]]; then
  echo "error: SDK tag verification failed after checkout" >&2
  exit 1
fi

if git -C "${SDK_DIR}" submodule status --recursive | grep -Eq '^[-+U]'; then
  echo "error: one or more SDK submodules are not at the recorded revision" >&2
  exit 1
fi

echo "Prepared ReXGlue SDK ${SDK_TAG} at ${SDK_DIR}"
echo "Note: upstream SDK source is not Android-enabled without a separately verified adaptation."
