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

#include "debug_checks.h"

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_debugger_internal_DebugNativeBridge_nativeTracerPidCheck(JNIEnv*, jclass) {
    char evidence[128] = {0};
    return mobshield_tracerpid_check(evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_debugger_internal_DebugNativeBridge_nativeTracerPidEvidence(JNIEnv* env, jclass) {
    char evidence[128] = {0};
    mobshield_tracerpid_check(evidence, sizeof(evidence));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_debugger_internal_DebugNativeBridge_nativePtraceSelf(JNIEnv*, jclass) {
    char evidence[128] = {0};
    return mobshield_ptrace_self(evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_debugger_internal_DebugNativeBridge_nativePtraceEvidence(JNIEnv* env, jclass) {
    char evidence[128] = {0};
    mobshield_ptrace_self(evidence, sizeof(evidence));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_debugger_internal_DebugNativeBridge_nativeTimingCheck(JNIEnv*, jclass) {
    char evidence[128] = {0};
    return mobshield_debugger_timing_check(evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_debugger_internal_DebugNativeBridge_nativeTimingEvidence(JNIEnv* env, jclass) {
    char evidence[128] = {0};
    mobshield_debugger_timing_check(evidence, sizeof(evidence));
    return env->NewStringUTF(evidence);
}
