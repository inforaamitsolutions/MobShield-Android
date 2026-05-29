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

#ifndef MOBSHIELD_PROLOGUE_CRYPTO_H
#define MOBSHIELD_PROLOGUE_CRYPTO_H

#include "mobshield_hooks_buildinfo.h"

#include <cstddef>
#include <cstdint>

inline void mobshield_derive_key(int fn_index, unsigned char* out_key, int key_len) {
    uint32_t state = MOBSHIELD_HOOKS_ENTROPY_SEED_HIGH ^ MOBSHIELD_HOOKS_ENTROPY_SEED_LOW ^
                     static_cast<uint32_t>(fn_index * 0x9E3779B9u);
    for (int i = 0; i < key_len; ++i) {
        state ^= state << 13;
        state ^= state >> 17;
        state ^= state << 5;
        out_key[i] = static_cast<unsigned char>(state & 0xFFu);
    }
}

inline bool mobshield_baseline_is_unset(const unsigned char* encrypted, int len, int fn_index) {
    unsigned char key[16];
    mobshield_derive_key(fn_index, key, 16);
    int unset_count = 0;
    for (int i = 0; i < len; ++i) {
        const unsigned char plain = static_cast<unsigned char>(encrypted[i] ^ key[i % 16]);
        if (plain == 0xFF) {
            ++unset_count;
        }
    }
    return unset_count == len;
}

inline void mobshield_decrypt_baseline(
    const unsigned char* encrypted,
    int fn_index,
    unsigned char* out,
    int len) {
    unsigned char key[16];
    mobshield_derive_key(fn_index, key, 16);
    for (int i = 0; i < len; ++i) {
        out[i] = static_cast<unsigned char>(encrypted[i] ^ key[i % 16]);
    }
}

#endif  // MOBSHIELD_PROLOGUE_CRYPTO_H
