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

#include "emu_checks.h"

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_environment_internal_EmuNativeBridge_nativeQemuProps(JNIEnv*, jclass) {
    char evidence[128] = {0};
    return mobshield_qemu_props(evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_environment_internal_EmuNativeBridge_nativeQemuPropsEvidence(JNIEnv* env, jclass) {
    char evidence[128] = {0};
    mobshield_qemu_props(evidence, sizeof(evidence));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_environment_internal_EmuNativeBridge_nativeQemuDevices(JNIEnv*, jclass) {
    char evidence[128] = {0};
    return mobshield_qemu_devices(evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_environment_internal_EmuNativeBridge_nativeQemuDevicesEvidence(JNIEnv* env, jclass) {
    char evidence[128] = {0};
    mobshield_qemu_devices(evidence, sizeof(evidence));
    return env->NewStringUTF(evidence);
}

extern "C" JNIEXPORT jint JNICALL
Java_io_mobshield_detect_environment_internal_EmuNativeBridge_nativeCpuFeatures(JNIEnv*, jclass) {
    char evidence[128] = {0};
    return mobshield_cpu_features(evidence, sizeof(evidence));
}

extern "C" JNIEXPORT jstring JNICALL
Java_io_mobshield_detect_environment_internal_EmuNativeBridge_nativeCpuFeaturesEvidence(JNIEnv* env, jclass) {
    char evidence[128] = {0};
    mobshield_cpu_features(evidence, sizeof(evidence));
    return env->NewStringUTF(evidence);
}
