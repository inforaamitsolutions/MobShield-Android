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

#ifndef MOBSHIELD_HOOKS_CHECKS_H
#define MOBSHIELD_HOOKS_CHECKS_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

#define MOBSHIELD_HOOKS_OK 0
#define MOBSHIELD_HOOKS_DETECTED 1
#define MOBSHIELD_HOOKS_UNAVAILABLE 2
#define MOBSHIELD_HOOKS_ERROR -1

int mobshield_proc_maps_scan(char* evidence, int evidence_len);
int mobshield_function_prologue_inspect(char* evidence, int evidence_len);
int mobshield_frida_port_probe(char* evidence, int evidence_len);
int mobshield_thread_name_scan(char* evidence, int evidence_len);
int mobshield_art_inspection(JNIEnv* env, jobject class_loader, char* evidence, int evidence_len);

#ifdef __cplusplus
}
#endif

#endif  // MOBSHIELD_HOOKS_CHECKS_H
