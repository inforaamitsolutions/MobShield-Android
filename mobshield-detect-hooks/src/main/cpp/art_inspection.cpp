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

#include <cstdio>
#include <cstring>

namespace {

bool try_load_class(JNIEnv* env, jobject class_loader, const char* class_name) {
    jclass loader_class = env->GetObjectClass(class_loader);
    if (loader_class == nullptr) {
        return false;
    }
    jmethodID load_class =
        env->GetMethodID(loader_class, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    if (load_class == nullptr) {
        env->DeleteLocalRef(loader_class);
        return false;
    }
    jstring name = env->NewStringUTF(class_name);
    if (name == nullptr) {
        env->DeleteLocalRef(loader_class);
        return false;
    }
    jobject loaded = env->CallObjectMethod(class_loader, load_class, name);
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
        env->DeleteLocalRef(name);
        env->DeleteLocalRef(loader_class);
        return false;
    }
    const bool found = loaded != nullptr;
    if (loaded != nullptr) {
        env->DeleteLocalRef(loaded);
    }
    env->DeleteLocalRef(name);
    env->DeleteLocalRef(loader_class);
    return found;
}

}  // namespace

int mobshield_art_inspection(JNIEnv* env, jobject class_loader, char* evidence, int evidence_len) {
    if (env == nullptr || class_loader == nullptr) {
        return MOBSHIELD_HOOKS_UNAVAILABLE;
    }

    static const char* kClasses[] = {
        "de.robv.android.xposed.XposedBridge",
        "org.lsposed.lspd.core.Main",
        "io.github.lsposed.lspd.core.Main",
        "de.robv.android.xposed.XposedHelpers",
        nullptr,
    };

    for (int i = 0; kClasses[i] != nullptr; ++i) {
        if (try_load_class(env, class_loader, kClasses[i])) {
            if (evidence != nullptr && evidence_len > 0) {
                snprintf(evidence, static_cast<size_t>(evidence_len), "class:%s", kClasses[i]);
            }
            return MOBSHIELD_HOOKS_DETECTED;
        }
    }

    return MOBSHIELD_HOOKS_OK;
}
