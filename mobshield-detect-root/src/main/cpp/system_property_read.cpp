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

#include <cstring>

#ifndef PROP_VALUE_MAX
#define PROP_VALUE_MAX 92
#endif

#if defined(__ANDROID__)
extern "C" int __system_property_get(const char* name, char* value);
#endif

int mobshield_read_system_property(const char* key, char* out, int out_len) {
    if (key == nullptr || out == nullptr || out_len <= 0) {
        return MOBSHIELD_ROOT_ERROR;
    }
#if defined(__ANDROID__)
    char buffer[PROP_VALUE_MAX] = {0};
    const int length = __system_property_get(key, buffer);
    if (length <= 0) {
        out[0] = '\0';
        return MOBSHIELD_ROOT_OK;
    }
    std::strncpy(out, buffer, static_cast<size_t>(out_len) - 1);
    out[out_len - 1] = '\0';
    return MOBSHIELD_ROOT_OK;
#else
    (void)key;
    out[0] = '\0';
    return MOBSHIELD_ROOT_OK;
#endif
}

#ifndef PROP_VALUE_MAX
#define PROP_VALUE_MAX 92
#endif
