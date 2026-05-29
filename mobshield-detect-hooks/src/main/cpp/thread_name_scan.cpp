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

#include <dirent.h>
#include <cstdio>
#include <cstring>

namespace {

bool matches_thread_name(const char* name) {
    static const char* kNames[] = {
        "gmain",
        "gdbus",
        "gum-js-loop",
        "pool-frida",
        "frida-agent",
        "linjector",
        nullptr,
    };
    for (int i = 0; kNames[i] != nullptr; ++i) {
        if (std::strcmp(name, kNames[i]) == 0 || std::strstr(name, kNames[i]) != nullptr) {
            return true;
        }
    }
    return false;
}

}  // namespace

int mobshield_thread_name_scan(char* evidence, int evidence_len) {
    DIR* tasks = opendir("/proc/self/task");
    if (tasks == nullptr) {
        return MOBSHIELD_HOOKS_UNAVAILABLE;
    }

    struct dirent* entry = nullptr;
    while ((entry = readdir(tasks)) != nullptr) {
        if (entry->d_name[0] < '0' || entry->d_name[0] > '9') {
            continue;
        }
        char path[128];
        snprintf(path, sizeof(path), "/proc/self/task/%s/comm", entry->d_name);
        FILE* comm = std::fopen(path, "r");
        if (comm == nullptr) {
            continue;
        }
        char name[64] = {0};
        if (std::fgets(name, sizeof(name), comm) != nullptr) {
            name[strcspn(name, "\n")] = '\0';
            if (matches_thread_name(name)) {
                if (evidence != nullptr && evidence_len > 0) {
                    snprintf(evidence, static_cast<size_t>(evidence_len), "thread:%s", name);
                }
                std::fclose(comm);
                closedir(tasks);
                return MOBSHIELD_HOOKS_DETECTED;
            }
        }
        std::fclose(comm);
    }

    closedir(tasks);
    return MOBSHIELD_HOOKS_OK;
}
