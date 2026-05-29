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
#include "prologue_crypto.h"

#include <cstdint>
#include <cstdio>
#include <cstring>
#include <dlfcn.h>

namespace {

struct TargetFn {
    const char* name;
    const unsigned char* encrypted_baseline;
    int fn_index;
};

bool is_arm64_hook_trampoline(const unsigned char* bytes, int len) {
#if defined(__aarch64__)
    if (len < 4) {
        return false;
    }
    const uint32_t insn = static_cast<uint32_t>(bytes[0]) | (static_cast<uint32_t>(bytes[1]) << 8) |
                          (static_cast<uint32_t>(bytes[2]) << 16) |
                          (static_cast<uint32_t>(bytes[3]) << 24);
    // B / BL immediate branch often used by inline hooks
    if ((insn & 0xFC000000u) == 0x14000000u) {
        return true;
    }
    // BR / BLR register branch
    if ((insn & 0xFFFFFC1Fu) == 0xD61F0000u) {
        return true;
    }
#endif
    (void)bytes;
    (void)len;
    return false;
}

bool compare_baseline(
    void* symbol,
    const unsigned char* encrypted,
    int fn_index,
    char* evidence,
    int evidence_len,
    const char* fn_name) {
    if (symbol == nullptr) {
        return false;
    }
    unsigned char live[MOBSHIELD_PROLOGUE_BYTES];
    std::memcpy(live, symbol, MOBSHIELD_PROLOGUE_BYTES);

    if (!mobshield_baseline_is_unset(encrypted, MOBSHIELD_PROLOGUE_BYTES, fn_index)) {
        unsigned char expected[MOBSHIELD_PROLOGUE_BYTES];
        mobshield_decrypt_baseline(encrypted, fn_index, expected, MOBSHIELD_PROLOGUE_BYTES);
        if (std::memcmp(live, expected, MOBSHIELD_PROLOGUE_BYTES) != 0) {
            if (evidence != nullptr && evidence_len > 0) {
                snprintf(evidence, static_cast<size_t>(evidence_len), "baseline:%s", fn_name);
            }
            return true;
        }
    }

    if (is_arm64_hook_trampoline(live, MOBSHIELD_PROLOGUE_BYTES)) {
        if (evidence != nullptr && evidence_len > 0) {
            snprintf(evidence, static_cast<size_t>(evidence_len), "trampoline:%s", fn_name);
        }
        return true;
    }
    return false;
}

}  // namespace

int mobshield_function_prologue_inspect(char* evidence, int evidence_len) {
    void* libc = dlopen("libc.so", RTLD_NOW | RTLD_NOLOAD);
    if (libc == nullptr) {
        libc = dlopen("libc.so", RTLD_NOW);
    }
    if (libc == nullptr) {
        return MOBSHIELD_HOOKS_UNAVAILABLE;
    }

    const TargetFn targets[] = {
        {"open", kEncryptedPrologueOpen, 0},
        {"read", kEncryptedPrologueRead, 1},
        {"syscall", kEncryptedPrologueSyscall, 2},
        {"android_dlopen_ext", kEncryptedPrologueDlopen, 3},
    };

    for (const auto& target : targets) {
        void* symbol = dlsym(libc, target.name);
        if (symbol == nullptr && std::strcmp(target.name, "android_dlopen_ext") == 0) {
            symbol = dlsym(libc, "dlopen");
        }
        if (compare_baseline(symbol, target.encrypted_baseline, target.fn_index, evidence, evidence_len, target.name)) {
            dlclose(libc);
            return MOBSHIELD_HOOKS_DETECTED;
        }
    }

    dlclose(libc);
    return MOBSHIELD_HOOKS_OK;
}
