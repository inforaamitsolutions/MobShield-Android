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

# Regenerates mobshield-spec/signals.json for a release tag.
# Usage: ./scripts/generate-signals-json.sh 1.2.3 signals-2026.06.0

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_ROOT="$(cd "${ROOT}/.." && pwd)"
API_VERSION="${1:-0.1.0}"
SIGNAL_SET="${2:-signals-2026.05.0}"
RELEASED_AT="$(date -u +%Y-%m-%d)"
OUT="${REPO_ROOT}/mobshield-spec/signals.json"

python3 - <<PY
import json
from pathlib import Path

out = Path("${OUT}")
data = {
    "signalSetVersion": "${SIGNAL_SET}",
    "apiVersion": "${API_VERSION}",
    "releasedAt": "${RELEASED_AT}",
    "description": "MobShield detection signal-set metadata. Bump signalSetVersion when probe weights, names, or logic change materially.",
    "modules": [
        "mobshield-core",
        "mobshield-detect-root",
        "mobshield-detect-hooks",
        "mobshield-detect-debugger",
        "mobshield-detect-environment",
        "mobshield-detect-integrity",
    ],
    "platforms": {
        "android": {
            "artifactPrefix": "io.mobshield",
            "reproducibleArtifacts": [
                "mobshield-core (Kotlin/Java wrapper, generic AAR without app personalization)",
                "mobshield-detect-* (Kotlin wrappers)",
            ],
            "nonReproducibleArtifacts": [
                "libmobshield*.so in consumer apps after io.mobshield.personalize",
            ],
        },
        "ios": {
            "reproducibleArtifacts": [
                "MobShield Swift wrappers (source distribution via SPM/CocoaPods)",
            ],
            "nonReproducibleArtifacts": [
                "MobShieldCoreNative and detect native slices after personalize build phase",
            ],
        },
    },
    "references": {
        "catalog": "mobshield-spec/DETECTION_CATALOG.md",
        "spec": "mobshield-spec/MOBSHIELD_SPEC.md",
    },
}
out.write_text(json.dumps(data, indent=2) + "\n")
print(f"Wrote {out}")
PY
