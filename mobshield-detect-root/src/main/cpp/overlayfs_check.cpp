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

bool is_critical_path(const char* mount_point) {
    return std::strncmp(mount_point, "/system", 7) == 0 ||
           std::strncmp(mount_point, "/vendor", 7) == 0 ||
           std::strncmp(mount_point, "/sbin", 5) == 0;
}

bool is_suspicious_fs(const char* fs_type, const char* options) {
    if (options != nullptr) {
        if (std::strstr(options, "magisk") != nullptr || std::strstr(options, "Magisk") != nullptr) {
            return true;
        }
    }
    if (std::strstr(fs_type, "tmpfs") != nullptr) {
        return true;
    }
    if (std::strstr(fs_type, "overlay") != nullptr && options != nullptr &&
        std::strstr(options, "magisk") != nullptr) {
        return true;
    }
    return false;
}

}  // namespace

int mobshield_overlayfs_check(char* evidence, int evidence_len) {
    FILE* file = std::fopen("/proc/self/mounts", "r");
    if (file == nullptr) {
        return MOBSHIELD_ROOT_ERROR;
    }

    char line[512];
    while (std::fgets(line, sizeof(line), file) != nullptr) {
        char device[128] = {0};
        char mount_point[128] = {0};
        char fs_type[64] = {0};
        char options[256] = {0};
        if (std::sscanf(line, "%127s %127s %63s %255s", device, mount_point, fs_type, options) < 3) {
            continue;
        }
        if (!is_critical_path(mount_point)) {
            continue;
        }
        if (is_suspicious_fs(fs_type, options)) {
            if (evidence != nullptr && evidence_len > 0) {
                std::snprintf(
                    evidence,
                    static_cast<size_t>(evidence_len),
                    "%s:%s",
                    mount_point,
                    fs_type);
            }
            std::fclose(file);
            return MOBSHIELD_ROOT_DETECTED;
        }
    }

    std::fclose(file);
    return MOBSHIELD_ROOT_OK;
}
