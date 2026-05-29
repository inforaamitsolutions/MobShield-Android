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

bool line_is_zygisk_indicator(const char* line) {
    return std::strstr(line, "libzygisk") != nullptr || std::strstr(line, "zygisk.so") != nullptr ||
           std::strstr(line, "/zygisk/") != nullptr;
}

}  // namespace

int mobshield_zygisk_loader_scan(char* evidence, int evidence_len) {
    FILE* file = std::fopen("/proc/self/maps", "r");
    if (file == nullptr) {
        return MOBSHIELD_ROOT_ERROR;
    }

    char line[512];
    while (std::fgets(line, sizeof(line), file) != nullptr) {
        if (!line_is_zygisk_indicator(line)) {
            continue;
        }
        if (evidence != nullptr && evidence_len > 0) {
            line[255] = '\0';
            std::snprintf(evidence, static_cast<size_t>(evidence_len), "%s", line);
        }
        std::fclose(file);
        return MOBSHIELD_ROOT_DETECTED;
    }

    std::fclose(file);
    return MOBSHIELD_ROOT_OK;
}
