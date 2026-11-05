#!/usr/bin/env bash
# Prepare the exact ReXGlue SDK source revision used by this project.
# This script does not apply Android patches or build the SDK.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SDK_DIR="${ROOT}/thirdparty/rexglue-sdk"
SDK_URL="https://github.com/rexglue/rexglue-sdk.git"
SDK_TAG="v0.10.0"
SDK_COMMIT="f5337cdc947ff6d4c4196737e2c807a48f2a1fc2"

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

  actual_commit="$(git -C "${SDK_DIR}" rev-parse HEAD)"
  if [[ "${actual_commit}" != "${SDK_COMMIT}" ]]; then
    echo "error: SDK checkout is not the pinned commit ${SDK_COMMIT} (found ${actual_commit}); refusing to reset it" >&2
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

actual_commit="$(git -C "${SDK_DIR}" rev-parse HEAD)"
if [[ "${actual_commit}" != "${SDK_COMMIT}" ]]; then
  echo "error: SDK commit verification failed (expected ${SDK_COMMIT}, found ${actual_commit})" >&2
  exit 1
fi

if git -C "${SDK_DIR}" submodule status --recursive | grep -Eq '^[-+U]'; then
  echo "error: one or more SDK submodules are not at the recorded revision" >&2
  exit 1
fi

echo "Prepared ReXGlue SDK ${SDK_TAG} (${SDK_COMMIT}) at ${SDK_DIR}"
echo "Note: upstream SDK source is not Android-enabled without a separately verified adaptation."
