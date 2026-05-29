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
#include <unistd.h>

int mobshield_qemu_devices(char* evidence, int evidence_len) {
    static const char* kPaths[] = {
        "/dev/socket/qemud",
        "/dev/qemu_pipe",
        "/dev/goldfish_pipe",
        nullptr,
    };

    for (int i = 0; kPaths[i] != nullptr; ++i) {
        if (access(kPaths[i], F_OK) == 0) {
            if (evidence != nullptr && evidence_len > 0) {
                snprintf(evidence, static_cast<size_t>(evidence_len), "path:%s", kPaths[i]);
            }
            return MOBSHIELD_EMU_DETECTED;
        }
    }
    return MOBSHIELD_EMU_OK;
}
