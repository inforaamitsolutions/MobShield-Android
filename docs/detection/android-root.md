# Android Root Detection (mobshield-detect-root)

This module implements nine independent checks that each emit at most one `Signal`. Signals are aggregated by `mobshield-core` into `PRIVILEGED_ACCESS` threat events.

## Signal catalog

| Signal | Technique | Default weight | Default confidence |
|--------|-----------|----------------|------------------|
| `android.root.mount_namespace` | PID 1 vs self mountinfo diff | 75 | 85 |
| `android.root.magisk_uds` | Magisk abstract UDS in `/proc/net/unix` | 70 | 80 |
| `android.root.overlayfs` | tmpfs/overlay/Magisk bind on `/system`, `/vendor`, `/sbin` | 65 | 75 |
| `android.root.errno_deviation` | Unexpected `stat()` errno on blocked paths | 60 | 70 |
| `android.root.zygisk_maps` | Zygisk loader lines in `/proc/self/maps` | 70 | 80 |
| `android.root.kernelsu_sysfs` | KernelSU paths and maps artifacts | 75 | 85 |
| `android.root.path_probe` | `su` file existence (no shell) | 25 | 35 |
| `android.root.dangerous_packages` | Known root/hook manager packages | 55 | 70 |
| `android.root.props` | Anomalous `ro.debuggable`, `ro.secure`, `ro.build.tags` | 20 | 30 |

Override weights via `MobShieldConfig.detectionTuning` keyed by signal name.

## Native checks (`libmobshieldroot.so`)

### 1. mount_namespace_check.cpp

Compares mount points in `/proc/1/mountinfo` vs `/proc/self/mountinfo`. Flags paths visible to init but not the app when they match Magisk, Zygisk, KernelSU, or hidden `/system` bind patterns.

**Survives:** basic `su` hiding, package hiding.

**Bypass:** Shamiko whitelist with full mount namespace concealment; careful per-app Zygisk hiding.

**Emulator note:** whitelists `/apex`, `/metadata`, `/linkerconfig`, `/product` to avoid Pixel AVD false positives.

### 2. magisk_uds_probe.cpp

Parses `/proc/net/unix` for socket names containing `magisk`, `zygisk`, or `@MAGISK`.

**Survives:** hidden manager UI, removed APK.

**Bypass:** renamed daemon socket, socket hiding modules.

### 3. overlayfs_check.cpp

Parses `/proc/self/mounts` for tmpfs or Magisk-tagged overlay mounts on critical partitions.

**Survives:** file-based `su` removal.

**Bypass:** mountless injection; Shamiko mount hiding.

### 4. errno_deviation.cpp

`stat()` on `/data/adb/magisk`, `/data/adb/ksu`, and known `su` paths. Detects existence or unexpected errno (not `EACCES`/`ENOENT`).

**Survives:** package-uninstall-only hides.

**Bypass:** path redirection; Shamiko `/proc` spoofing.

### 5. zygisk_loader_scan.cpp

Scans `/proc/self/maps` for `libzygisk`, `zygisk.so`, or `/zygisk/` paths only (no broad anonymous-RWX heuristic).

**Survives:** Magisk app hide.

**Bypass:** manual map cleaning; inline hooks without loader name.

### 6. kernelsu_check.cpp

Probes `/data/adb/ksu`, `/data/adb/ksud`, and KernelSU strings in maps.

**Survives:** Magisk-only hides.

**Bypass:** path rename; kernel-only mode without artifacts.

## Kotlin checks

### 7. SuPathScan.kt

Uses `File.exists()` on static paths and `PATH` directories. No `Runtime.exec`.

**Survives:** UI-only hides.

**Bypass:** Shamiko path hide; renamed `su`.

### 8. DangerousPackagesScan.kt

`PackageManager.getPackageInfo` for Magisk, KernelSU, Xposed, LSPosed, Substrate package IDs.

**Survives:** `su` deletion.

**Bypass:** Shamiko package hide; repackaged manager names.

### 9. SystemPropertiesProbe.kt

Reads `ro.debuggable`, `ro.secure`, `ro.build.tags` through native `__system_property_get` (no Kotlin reflection). Flags `debuggable=1`, `secure=0`, or `test-keys`.

**Survives:** post-boot prop reset tools (partial).

**Bypass:** resetprop hiding; OEM test images (tune confidence down).

## Integration

```kotlin
val config = MobShieldConfig(
    detectionTuning = mapOf(
        RootSignalDefaults.PATH_PROBE to SignalTuning(weight = 15, confidence = 25),
    ),
)
ModuleRegistry.register(RootDetectionModule(context, config.detectionTuning))
MobShield.start(context, config, listener)
```

## Constraints

- Each native probe is invoked from Kotlin with `withTimeout(50ms)`.
- No shell execution anywhere in this module.
- Acceptance target: no signals on stock Pixel emulator API 34/35 images with default Google Play image.
