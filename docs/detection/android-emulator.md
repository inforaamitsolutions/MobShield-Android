# Android Emulator and Automation Detection (mobshield-detect-environment)

This module implements six checks for QEMU emulators and UI automation frameworks. Signals aggregate to `EMULATOR` and `AUTOMATION` (MASVS-RES-4, MASVS-RES-10).

Weights are intentionally moderate so stock AVDs used by developers typically stay below CRITICAL on default thresholds.

## Signal catalog

| Signal | Technique | Default weight | Default confidence |
|--------|-----------|----------------|------------------|
| `android.env.qemu_props` | `ro.kernel.qemu`, `ro.hardware`, etc. | 22 | 65 |
| `android.env.qemu_device` | `/dev/qemu_pipe`, `qemud` socket | 28 | 75 |
| `android.env.cpu_goldfish` | `Hardware: Goldfish` / `Ranchu` in cpuinfo | 24 | 70 |
| `android.env.build_fingerprint` | `Build.FINGERPRINT` / product heuristics | 20 | 60 |
| `android.env.sensor_count` | `SensorManager` sensor list size | 18 | 55 |
| `android.automation.framework` | Appium / UiAutomator services | 30 | 70 |

Override weights via `MobShieldConfig.detectionTuning`.

## Native checks (`libmobshieldemu.so`)

### qemu_props.cpp

Uses `__system_property_get` for `ro.kernel.qemu`, `ro.hardware`, `ro.product.device`, and `ro.boot.qemu`.

### qemu_devices.cpp

`stat()` on `/dev/socket/qemud`, `/dev/qemu_pipe`, and `/dev/goldfish_pipe`.

### cpu_features.cpp

Parses `/proc/cpuinfo` for Goldfish or Ranchu hardware strings.

## Kotlin checks

### BuildFingerprintCheck.kt

Inspects fingerprint, manufacturer, product, model, brand, and hardware for emulator markers (`sdk_gphone`, `ranchu`, `generic`, etc.).

### SensorCountCheck.kt

Physical devices usually expose more sensors than minimal AVD images. Default threshold: fewer than 7 sensors.

### AutomationFrameworkCheck.kt

Scans running service class names and enabled accessibility services for `io.appium`, `uiautomator`, and `androidx.test`.

## Registration

```kotlin
EmulatorDetectionRegistrar.register(context, mobShieldConfig)
```

Module name: `environment`. Module criticality: `35` (lower than root/hooks/debug; emulator is expected in dev workflows).
