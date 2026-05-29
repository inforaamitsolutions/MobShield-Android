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

#include <jni.h>

#include <cstring>

#include "mobshield_native.h"

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    mobshield_native_init();
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_core_internal_NativeBridge_nativeGetVersion(JNIEnv* env, jclass /*clazz*/) {
    char buffer[64];
    if (mobshield_native_get_version(buffer, sizeof(buffer)) < 0) {
        return env->NewStringUTF("");
    }
    return env->NewStringUTF(buffer);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_core_internal_NativeBridge_nativeGetBuildId(JNIEnv* env, jclass /*clazz*/) {
    char buffer[128];
    if (mobshield_native_get_build_id(buffer, sizeof(buffer)) < 0) {
        return env->NewStringUTF("");
    }
    return env->NewStringUTF(buffer);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_core_internal_NativeBridge_nativeSelfCheck(JNIEnv* /*env*/, jclass /*clazz*/) {
    return mobshield_native_self_check();
}
