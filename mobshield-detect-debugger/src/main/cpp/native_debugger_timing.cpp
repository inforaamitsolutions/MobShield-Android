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
#include <ctime>

namespace {

long monotonic_ns() {
    timespec ts {};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return static_cast<long>(ts.tv_sec) * 1000000000L + ts.tv_nsec;
}

}  // namespace

int mobshield_debugger_timing_check(char* evidence, int evidence_len) {
    const long baseline_start = monotonic_ns();
    const long baseline_end = monotonic_ns();
    const long baseline_delta = baseline_end - baseline_start;

    const long loop_start = monotonic_ns();
    volatile unsigned long accumulator = 0;
    for (int i = 0; i < 50000; ++i) {
        accumulator += static_cast<unsigned long>(i * i);
    }
    const long loop_end = monotonic_ns();
    const long loop_delta = loop_end - loop_start;

    (void)accumulator;

    // Debugger single-stepping inflates loop delta relative to empty baseline.
    if (loop_delta > baseline_delta * 200 && loop_delta > 5000000L) {
        if (evidence != nullptr && evidence_len > 0) {
            snprintf(evidence, static_cast<size_t>(evidence_len), "delta_ns:%ld", loop_delta);
        }
        return MOBSHIELD_DEBUG_DETECTED;
    }
    return MOBSHIELD_DEBUG_OK;
}
