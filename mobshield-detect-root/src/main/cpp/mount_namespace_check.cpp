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
#include <set>
#include <string>
#include <vector>

namespace {

constexpr int kMaxFileBytes = 256 * 1024;

bool is_emulator_whitelisted_path(const char* path) {
    static const char* kWhitelist[] = {
        "/apex",
        "/metadata",
        "/linkerconfig",
        "/product",
        "/system/product",
        "/postinstall",
        "/oem",
        nullptr,
    };
    for (int i = 0; kWhitelist[i] != nullptr; ++i) {
        if (std::strncmp(path, kWhitelist[i], std::strlen(kWhitelist[i])) == 0) {
            return true;
        }
    }
    return false;
}

bool is_suspicious_mount_token(const char* token) {
    static const char* kTokens[] = {"magisk", "zygisk", "/data/adb", "kernelsu", nullptr};
    for (int i = 0; kTokens[i] != nullptr; ++i) {
        if (std::strstr(token, kTokens[i]) != nullptr) {
            return true;
        }
    }
    return false;
}

bool read_mountinfo_paths(const char* proc_path, std::set<std::string>& out_paths) {
    FILE* file = std::fopen(proc_path, "r");
    if (file == nullptr) {
        return false;
    }
    char line[1024];
    int bytes_read = 0;
    while (std::fgets(line, sizeof(line), file) != nullptr) {
        bytes_read += static_cast<int>(std::strlen(line));
        if (bytes_read > kMaxFileBytes) {
            break;
        }
        // mountinfo: fields before " - " include mount point as 5th token
        char* separator = std::strstr(line, " - ");
        if (separator == nullptr) {
            continue;
        }
        *separator = '\0';
        char* tokens[8] = {nullptr};
        int token_count = 0;
        char* cursor = line;
        while (token_count < 8) {
            char* next = std::strchr(cursor, ' ');
            if (next != nullptr) {
                *next = '\0';
            }
            tokens[token_count++] = cursor;
            if (next == nullptr) {
                break;
            }
            cursor = next + 1;
        }
        if (token_count >= 5 && tokens[4] != nullptr) {
            out_paths.insert(tokens[4]);
        }
    }
    std::fclose(file);
    return true;
}

void write_evidence(char* evidence, int evidence_len, const char* message) {
    if (evidence == nullptr || evidence_len <= 0) {
        return;
    }
    std::snprintf(evidence, static_cast<size_t>(evidence_len), "%s", message);
}

}  // namespace

int mobshield_mount_namespace_check(char* evidence, int evidence_len) {
    std::set<std::string> init_paths;
    std::set<std::string> self_paths;
    if (!read_mountinfo_paths("/proc/1/mountinfo", init_paths) ||
        !read_mountinfo_paths("/proc/self/mountinfo", self_paths)) {
        return MOBSHIELD_ROOT_ERROR;
    }

    for (const auto& path : init_paths) {
        if (self_paths.find(path) != self_paths.end()) {
            continue;
        }
        if (is_emulator_whitelisted_path(path.c_str())) {
            continue;
        }
        if (is_suspicious_mount_token(path.c_str())) {
            write_evidence(evidence, evidence_len, path.c_str());
            return MOBSHIELD_ROOT_DETECTED;
        }
    }

    return MOBSHIELD_ROOT_OK;
}
