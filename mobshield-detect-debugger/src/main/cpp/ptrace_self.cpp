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

#include <cerrno>
#include <cstdio>
#include <cstring>
#include <sys/ptrace.h>
#include <unistd.h>

int mobshield_ptrace_self(char* evidence, int evidence_len) {
    errno = 0;
    const long result = ptrace(PTRACE_TRACEME, 0, nullptr, 0);
    if (result == 0) {
        return MOBSHIELD_DEBUG_OK;
    }
    if (errno == EPERM || errno == EBUSY || errno == EACCES) {
        if (evidence != nullptr && evidence_len > 0) {
            snprintf(evidence, static_cast<size_t>(evidence_len), "errno:%d", errno);
        }
        return MOBSHIELD_DEBUG_DETECTED;
    }
    return MOBSHIELD_DEBUG_OK;
}
