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

#ifndef PROP_VALUE_MAX
#define PROP_VALUE_MAX 92
#endif

#if defined(__ANDROID__)
extern "C" int __system_property_get(const char* name, char* value);
#endif

int mobshield_read_system_property(const char* key, char* out, int out_len) {
    if (key == nullptr || out == nullptr || out_len <= 0) {
        return MOBSHIELD_EMU_ERROR;
    }
#if defined(__ANDROID__)
    char buffer[PROP_VALUE_MAX] = {0};
    const int length = __system_property_get(key, buffer);
    if (length <= 0) {
        out[0] = '\0';
        return MOBSHIELD_EMU_OK;
    }
    strncpy(out, buffer, static_cast<size_t>(out_len) - 1);
    out[out_len - 1] = '\0';
    return MOBSHIELD_EMU_OK;
#else
    (void)key;
    out[0] = '\0';
    return MOBSHIELD_EMU_OK;
#endif
}

int mobshield_qemu_props(char* evidence, int evidence_len) {
    static const char* kKeys[] = {
        "ro.kernel.qemu",
        "ro.hardware",
        "ro.product.device",
        "ro.boot.qemu",
        nullptr,
    };

    for (int i = 0; kKeys[i] != nullptr; ++i) {
        char value[PROP_VALUE_MAX] = {0};
        mobshield_read_system_property(kKeys[i], value, sizeof(value));
        if (value[0] == '\0') {
            continue;
        }
        const char* lower_key = kKeys[i];
        if (strstr(value, "qemu") != nullptr || strstr(value, "goldfish") != nullptr ||
            strstr(value, "ranchu") != nullptr || strstr(value, "sdk_gphone") != nullptr ||
            (strcmp(lower_key, "ro.kernel.qemu") == 0 && value[0] == '1')) {
            if (evidence != nullptr && evidence_len > 0) {
                snprintf(evidence, static_cast<size_t>(evidence_len), "%s=%s", kKeys[i], value);
            }
            return MOBSHIELD_EMU_DETECTED;
        }
    }
    return MOBSHIELD_EMU_OK;
}
