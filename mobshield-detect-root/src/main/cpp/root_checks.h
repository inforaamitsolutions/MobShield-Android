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

#ifndef MOBSHIELD_ROOT_CHECKS_H
#define MOBSHIELD_ROOT_CHECKS_H

#ifdef __cplusplus
extern "C" {
#endif

#define MOBSHIELD_ROOT_OK 0
#define MOBSHIELD_ROOT_DETECTED 1
#define MOBSHIELD_ROOT_ERROR -1

int mobshield_mount_namespace_check(char* evidence, int evidence_len);
int mobshield_magisk_uds_probe(char* evidence, int evidence_len);
int mobshield_overlayfs_check(char* evidence, int evidence_len);
int mobshield_errno_deviation(char* evidence, int evidence_len);
int mobshield_zygisk_loader_scan(char* evidence, int evidence_len);
int mobshield_kernelsu_check(char* evidence, int evidence_len);
int mobshield_read_system_property(const char* key, char* out, int out_len);

#ifdef __cplusplus
}
#endif

#endif  // MOBSHIELD_ROOT_CHECKS_H
