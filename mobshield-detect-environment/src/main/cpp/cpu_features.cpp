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

#include "emu_checks.h"

#include <cstdio>
#include <cstring>

int mobshield_cpu_features(char* evidence, int evidence_len) {
    FILE* file = fopen("/proc/cpuinfo", "r");
    if (file == nullptr) {
        return MOBSHIELD_EMU_UNAVAILABLE;
    }

    char line[256];
    while (fgets(line, sizeof(line), file) != nullptr) {
        if (strstr(line, "Hardware") == nullptr) {
            continue;
        }
        if (strstr(line, "Goldfish") != nullptr || strstr(line, "Ranchu") != nullptr ||
            strstr(line, "ranchu") != nullptr || strstr(line, "goldfish") != nullptr) {
            if (evidence != nullptr && evidence_len > 0) {
                line[strcspn(line, "\n")] = '\0';
                snprintf(evidence, static_cast<size_t>(evidence_len), "%s", line);
            }
            fclose(file);
            return MOBSHIELD_EMU_DETECTED;
        }
    }

    fclose(file);
    return MOBSHIELD_EMU_OK;
}
