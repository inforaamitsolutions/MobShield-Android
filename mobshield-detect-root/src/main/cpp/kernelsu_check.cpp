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
#include <sys/stat.h>
#include <unistd.h>

namespace {

bool path_exists(const char* path) {
    return access(path, F_OK) == 0;
}

bool maps_contains_kernelsu() {
    FILE* file = std::fopen("/proc/self/maps", "r");
    if (file == nullptr) {
        return false;
    }
    char line[512];
    bool found = false;
    while (std::fgets(line, sizeof(line), file) != nullptr) {
        if (std::strstr(line, "kernelsu") != nullptr || std::strstr(line, "KernelSU") != nullptr) {
            found = true;
            break;
        }
    }
    std::fclose(file);
    return found;
}

}  // namespace

int mobshield_kernelsu_check(char* evidence, int evidence_len) {
    static const char* kPaths[] = {
        "/data/adb/ksu",
        "/data/adb/ksud",
    };

    for (const char* path : kPaths) {
        if (path_exists(path)) {
            if (evidence != nullptr && evidence_len > 0) {
                std::snprintf(evidence, static_cast<size_t>(evidence_len), "path:%s", path);
            }
            return MOBSHIELD_ROOT_DETECTED;
        }
    }

    if (maps_contains_kernelsu()) {
        if (evidence != nullptr && evidence_len > 0) {
            std::snprintf(evidence, static_cast<size_t>(evidence_len), "%s", "maps:kernelsu");
        }
        return MOBSHIELD_ROOT_DETECTED;
    }

    return MOBSHIELD_ROOT_OK;
}
