#!/usr/bin/env bash
# Copyright 2025 MobShield Contributors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Builds mobshield-core release AAR twice with fixed SOURCE_DATE_EPOCH and compares contents.
# Allowed differences: META-INF timestamps, manifest entries tied to build time.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

export SOURCE_DATE_EPOCH="${SOURCE_DATE_EPOCH:-1700000000}"
MODULE="${1:-mobshield-core}"
WORKDIR="${ROOT}/build/repro-verify"
rm -rf "${WORKDIR}"
mkdir -p "${WORKDIR}"

echo "Reproducibility check for :${MODULE}:release (SOURCE_DATE_EPOCH=${SOURCE_DATE_EPOCH})"

build_once() {
  local label="$1"
  local out="${WORKDIR}/${label}"
  mkdir -p "${out}"
  ./gradlew ":${MODULE}:clean" ":${MODULE}:bundleReleaseAar" --no-daemon -q
  local aar
  aar="$(find "${MODULE}/build/outputs/aar" -name '*-release.aar' | head -1)"
  if [[ ! -f "${aar}" ]]; then
    echo "error: release AAR not found for ${MODULE}" >&2
    exit 1
  fi
  unzip -q "${aar}" -d "${out}/extracted"
  (cd "${out}/extracted" && find . -type f | sort | while read -r f; do
    shasum -a 256 "${f}"
  done) > "${out}/hashes.txt"
}

build_once "build1"
./gradlew ":${MODULE}:clean" --no-daemon -q
build_once "build2"

IGNORE_REGEX='META-INF/|manifest|pom\.properties|module\.json'

if diff -u "${WORKDIR}/build1/hashes.txt" "${WORKDIR}/build2/hashes.txt" > "${WORKDIR}/diff.txt"; then
  echo "PASS: AAR file hashes match between builds"
  exit 0
fi

echo "Hash diff (before filtering allowed paths):"
cat "${WORKDIR}/diff.txt"

FILTERED_DIFF="$(grep -E '^[+-]' "${WORKDIR}/diff.txt" | grep -Ev "${IGNORE_REGEX}" || true)"
if [[ -z "${FILTERED_DIFF}" ]]; then
  echo "PASS: only allowed metadata differences"
  exit 0
fi

echo "FAIL: non-metadata differences detected"
echo "${FILTERED_DIFF}"
exit 1
