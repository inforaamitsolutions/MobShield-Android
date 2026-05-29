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

#include "root_checks.h"

#include <cstdio>
#include <cstring>

namespace {

bool matches_magisk_socket(const char* line) {
    static const char* kPatterns[] = {
        "magisk",
        "zygisk",
        "@MAGISK",
        "@magisk",
        "adbd-magisk",
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

int mobshield_magisk_uds_probe(char* evidence, int evidence_len) {
    FILE* file = std::fopen("/proc/net/unix", "r");
    if (file == nullptr) {
        return MOBSHIELD_ROOT_ERROR;
    }

    char line[512];
    // Skip header
    if (std::fgets(line, sizeof(line), file) == nullptr) {
        std::fclose(file);
        return MOBSHIELD_ROOT_ERROR;
    }

    while (std::fgets(line, sizeof(line), file) != nullptr) {
        if (matches_magisk_socket(line)) {
            if (evidence != nullptr && evidence_len > 0) {
                std::snprintf(evidence, static_cast<size_t>(evidence_len), "%s", line);
            }
            std::fclose(file);
            return MOBSHIELD_ROOT_DETECTED;
        }
    }

    std::fclose(file);
    return MOBSHIELD_ROOT_OK;
}
