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

#include <cerrno>
#include <cstdio>
#include <cstring>
#include <sys/stat.h>
#include <unistd.h>

namespace {

struct ProbePath {
    const char* path;
    bool expect_eacces_when_hidden;
};

bool probe_path(const ProbePath& probe, char* evidence, int evidence_len) {
    errno = 0;
    struct stat buffer {};
    const int result = stat(probe.path, &buffer);
    if (result == 0) {
        if (evidence != nullptr && evidence_len > 0) {
            std::snprintf(evidence, static_cast<size_t>(evidence_len), "exists:%s", probe.path);
        }
        return true;
    }
    const int err = errno;
    if (probe.expect_eacces_when_hidden && err != EACCES && err != ENOENT) {
        if (evidence != nullptr && evidence_len > 0) {
            std::snprintf(
                evidence,
                static_cast<size_t>(evidence_len),
                "errno:%s:%d",
                probe.path,
                err);
        }
        return true;
    }
    return false;
}

}  // namespace

int mobshield_errno_deviation(char* evidence, int evidence_len) {
    static const ProbePath kPaths[] = {
        {"/data/adb/magisk", true},
        {"/data/adb/ksu", true},
        {"/sbin/su", true},
        {"/system/bin/su", true},
        {"/system/xbin/su", true},
    };

    for (const auto& probe : kPaths) {
        if (probe_path(probe, evidence, evidence_len)) {
            return MOBSHIELD_ROOT_DETECTED;
        }
    }
    return MOBSHIELD_ROOT_OK;
}
