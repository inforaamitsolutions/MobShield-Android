/*
 * Copyright 2025 MobShield Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "hooks_checks.h"

#include <cstdio>
#include <cstring>

namespace {

bool matches_maps_indicator(const char* line) {
    static const char* kPatterns[] = {
        "frida-agent",
        "frida-gadget",
        "gum-js-loop",
        "linjector",
        "libfrida",
        "frida-server",
        "XposedBridge.jar",
        "/data/adb/lspd",
        "libxposed",
        "liblsposed",
        nullptr,
    };
    for (int i = 0; kPatterns[i] != nullptr; ++i) {
        if (std::strstr(line, kPatterns[i]) != nullptr) {
            return true;
        }
    }
    return false;
}

}  // namespace

int mobshield_proc_maps_scan(char* evidence, int evidence_len) {
    FILE* file = std::fopen("/proc/self/maps", "r");
    if (file == nullptr) {
        return MOBSHIELD_HOOKS_UNAVAILABLE;
    }

    char line[512];
    while (std::fgets(line, sizeof(line), file) != nullptr) {
        if (!matches_maps_indicator(line)) {
            continue;
        }
        if (evidence != nullptr && evidence_len > 0) {
            line[255] = '\0';
            snprintf(evidence, static_cast<size_t>(evidence_len), "%s", line);
        }
        std::fclose(file);
        return MOBSHIELD_HOOKS_DETECTED;
    }

    std::fclose(file);
    return MOBSHIELD_HOOKS_OK;
}
