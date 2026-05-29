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

#include "debug_checks.h"

#include <cstdio>
#include <cstring>

int mobshield_tracerpid_check(char* evidence, int evidence_len) {
    FILE* file = std::fopen("/proc/self/status", "r");
    if (file == nullptr) {
        return MOBSHIELD_DEBUG_UNAVAILABLE;
    }

    char line[256];
    while (std::fgets(line, sizeof(line), file) != nullptr) {
        if (std::strncmp(line, "TracerPid:", 10) != 0) {
            continue;
        }
        int tracer_pid = 0;
        if (std::sscanf(line, "TracerPid:\t%d", &tracer_pid) == 1 && tracer_pid > 0) {
            if (evidence != nullptr && evidence_len > 0) {
                snprintf(evidence, static_cast<size_t>(evidence_len), "pid:%d", tracer_pid);
            }
            std::fclose(file);
            return MOBSHIELD_DEBUG_DETECTED;
        }
        break;
    }

    std::fclose(file);
    return MOBSHIELD_DEBUG_OK;
}
