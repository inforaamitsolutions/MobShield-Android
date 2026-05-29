# Android Debugger Detection (mobshield-detect-debugger)

This module implements six checks that emit `Signal` values aggregated into `DEBUGGER` and `ADB_ENABLED` threat events (MASVS-RES-7).

## Signal catalog

| Signal | Technique | Default weight | Default confidence |
|--------|-----------|----------------|------------------|
| `android.debug.tracerpid` | `/proc/self/status` TracerPid | 85 | 90 |
| `android.debug.ptrace` | `PTRACE_TRACEME` / EPERM heuristic | 80 | 85 |
| `android.debug.timing` | Monotonic timing anomaly | 65 | 70 |
| `android.debug.app_debuggable` | `FLAG_DEBUGGABLE` in release builds | 70 | 60 |
| `android.adb.enabled` | `ADB_ENABLED` / USB debugging settings | 30 | 40 |
| `android.debug.waiting` | `Debug.waitingForDebugger()` / `isDebuggerConnected()` | 90 | 95 |

`android.adb.*` maps to informational `ADB_ENABLED` (not CRITICAL by default). Other signals map to `DEBUGGER`.

Override weights via `MobShieldConfig.detectionTuning`.

## Native checks (`libmobshielddebug.so`)

### tracerpid_check.cpp

Reads `/proc/self/status` and parses `TracerPid`. Non-zero indicates an attached tracer.

### ptrace_self.cpp

Calls `ptrace(PTRACE_TRACEME, ...)`. Returns detected when attach fails with `EPERM` while another tracer is active.

### native_debugger_timing.cpp

Measures `clock_gettime(CLOCK_MONOTONIC)` over a short loop; unusually slow iterations suggest debugger single-stepping.

## Kotlin checks

### AppDebuggable.kt

Uses `ApplicationInfo.FLAG_DEBUGGABLE`. Weight is **70** when the app is a release variant (`BuildConfig.DEBUG == false`), **0** in debug variants (no signal).

### AdbEnabledCheck.kt

Reads `Settings.Global.ADB_ENABLED` and `adb_usb_enabled`. Low weight; common on developer devices.

### WaitingForDebugger.kt

Uses `Debug.waitingForDebugger()` and `Debug.isDebuggerConnected()`.

## Registration

```kotlin
DebugDetectionRegistrar.register(context, mobShieldConfig)
```

Module name: `debug`. Module criticality: `100` (scheduling priority; ADB remains informational at aggregation).
