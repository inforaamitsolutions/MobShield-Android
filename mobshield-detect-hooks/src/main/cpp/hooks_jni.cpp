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

#include "hooks_checks.h"

namespace {

int run_check(int (*check_fn)(char*, int), char* evidence, int evidence_len) {
    return check_fn(evidence, evidence_len);
}

}  // namespace

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeProcMapsScan(JNIEnv*, jclass) {
    char evidence[256] = {0};
    return run_check(mobshield_proc_maps_scan, evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeProcMapsEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_proc_maps_scan(evidence, static_cast<int>(sizeof(evidence)));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeFunctionPrologueInspect(JNIEnv*, jclass) {
    char evidence[256] = {0};
    return run_check(mobshield_function_prologue_inspect, evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeFunctionPrologueEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_function_prologue_inspect(evidence, static_cast<int>(sizeof(evidence)));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeFridaPortProbe(JNIEnv*, jclass) {
    char evidence[256] = {0};
    return run_check(mobshield_frida_port_probe, evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeFridaPortEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_frida_port_probe(evidence, static_cast<int>(sizeof(evidence)));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeThreadNameScan(JNIEnv*, jclass) {
    char evidence[256] = {0};
    return run_check(mobshield_thread_name_scan, evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeThreadNameEvidence(JNIEnv* env, jclass) {
    char evidence[256] = {0};
    mobshield_thread_name_scan(evidence, static_cast<int>(sizeof(evidence)));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeArtInspection(
    JNIEnv* env,
    jclass,
    jobject class_loader) {
    char evidence[256] = {0};
    return mobshield_art_inspection(env, class_loader, evidence, static_cast<int>(sizeof(evidence)));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_hooks_internal_HooksNativeBridge_nativeArtInspectionEvidence(
    JNIEnv* env,
    jclass,
    jobject class_loader) {
    char evidence[256] = {0};
    mobshield_art_inspection(env, class_loader, evidence, static_cast<int>(sizeof(evidence)));
    return env->NewStringUTF(evidence);
}
