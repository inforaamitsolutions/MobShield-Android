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

#ifndef PROP_VALUE_MAX
#define PROP_VALUE_MAX 92
#endif

#include "root_checks.h"

namespace {

jstring to_jstring(JNIEnv* env, const char* value) {
    if (value == nullptr) {
        return env->NewStringUTF("");
    }
    return env->NewStringUTF(value);
}

int run_check(int (*check_fn)(char*, int)) {
    char evidence[256] = {0};
    return check_fn(evidence, static_cast<int>(sizeof(evidence)));
}

}  // namespace

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeMountNamespaceCheck(JNIEnv* env, jclass) {
    return run_check(mobshield_mount_namespace_check);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeMountNamespaceEvidence(
    JNIEnv* env,
    jclass) {
    char evidence[256] = {0};
    mobshield_mount_namespace_check(evidence, static_cast<int>(sizeof(evidence)));
    return to_jstring(env, evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeMagiskUdsProbe(JNIEnv*, jclass) {
    return run_check(mobshield_magisk_uds_probe);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeMagiskUdsEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_magisk_uds_probe(evidence, static_cast<int>(sizeof(evidence)));
    return to_jstring(env, evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeOverlayfsCheck(JNIEnv*, jclass) {
    return run_check(mobshield_overlayfs_check);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeOverlayfsEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_overlayfs_check(evidence, static_cast<int>(sizeof(evidence)));
    return to_jstring(env, evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeErrnoDeviation(JNIEnv*, jclass) {
    return run_check(mobshield_errno_deviation);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeErrnoEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_errno_deviation(evidence, static_cast<int>(sizeof(evidence)));
    return to_jstring(env, evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeZygiskLoaderScan(JNIEnv*, jclass) {
    return run_check(mobshield_zygisk_loader_scan);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeZygiskEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_zygisk_loader_scan(evidence, static_cast<int>(sizeof(evidence)));
    return to_jstring(env, evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeKernelsuCheck(JNIEnv*, jclass) {
    return run_check(mobshield_kernelsu_check);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeKernelsuEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_kernelsu_check(evidence, static_cast<int>(sizeof(evidence)));
    return to_jstring(env, evidence);
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_root_internal_RootNativeBridge_nativeReadSystemProperty(
    JNIEnv* env,
    jclass,
    jstring key) {
    const char* key_chars = env->GetStringUTFChars(key, nullptr);
    char buffer[PROP_VALUE_MAX] = {0};
    mobshield_read_system_property(key_chars, buffer, static_cast<int>(sizeof(buffer)));
    env->ReleaseStringUTFChars(key, key_chars);
    return to_jstring(env, buffer);
}
