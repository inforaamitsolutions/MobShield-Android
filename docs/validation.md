# MobShield Android — Detection validation

This document defines **what each detector is expected to report**, so a run on a real device can
be checked against a known baseline instead of a guess. It is the reference for the validation
harness (`MobShieldValidation`) and any on-device procedure.

## Why this exists

Unit tests run on the JVM and cannot be rooted or hooked; instrumented tests run on an emulator or
a clean device. They prove the detectors do not crash and do not raise false positives in a clean
environment — they do **not** prove a detector fires when the threat is actually present. Confirming
positive detection requires running on a compromised device. This matrix + the harness make that
check repeatable.

## The validation harness

`MobShieldValidation` (in `mobshield-core`) runs the registered detection modules and returns a
`ValidationReport` exposing both layers:

- **Raw signals** — per module, exactly which `Signal`s fired, with weight/confidence/evidence.
- **Aggregated events** — the `ThreatEvent`s those signals map to, with severity and score.

```kotlin
// Register the modules you want to validate, then:
val report = MobShieldValidation.runRegistered()
report.modules.forEach { m -> println("${m.moduleName} fired=${m.didFire} ${m.signals.map { it.name }}") }
println("threats=${report.activeThreatTypes} severity=${report.highestSeverity}")
```

`report.firedSignalNames` and `report.activeThreatTypes` are the two things to compare against the
tables below. Exact weights/confidences live in each module's `*SignalDefaults.kt` (overridable via
`MobShieldConfig.detectionTuning`).

## Expected-signal matrix

### Root — module `root`, threat `PRIVILEGED_ACCESS`
`android.root.mount_namespace`, `android.root.magisk_uds`, `android.root.overlayfs`,
`android.root.errno_deviation`, `android.root.zygisk_maps`, `android.root.kernelsu_sysfs`,
`android.root.path_probe`, `android.root.dangerous_packages`, `android.root.props`

### Hooks — module `hooks`, threat `HOOK_FRAMEWORK`
`common.hook.frida_maps`, `common.hook.prologue`, `android.hook.frida_port`,
`android.hook.thread_name`, `android.hook.art_dex`, `android.hook.suspicious_library`,
`android.hook.xposed`, `android.hook.stack_leak`

### Debugger — module `debugger`, threat `DEBUGGER` (plus `ADB_ENABLED`)
`android.debug.tracerpid`, `android.debug.ptrace`, `android.debug.timing`,
`android.debug.app_debuggable`, `android.debug.waiting`, `android.adb.enabled` (→ `ADB_ENABLED`)

### Environment — module `environment`, threats `EMULATOR` / `AUTOMATION`
`android.env.qemu_props`, `android.env.qemu_device`, `android.env.cpu_goldfish`,
`android.env.build_fingerprint`, `android.env.sensor_count`,
`android.automation.framework` (→ `AUTOMATION`)

### Integrity — module `integrity`, threats `APP_INTEGRITY` / `UNOFFICIAL_STORE`
`android.integrity.signature`, `android.integrity.apk_checksum`,
`android.integrity.native_lib_checksum`, `android.integrity.native_self_check`,
`android.store.installer` (→ `UNOFFICIAL_STORE`)

## Environment baselines

### Clean environment (stock/emulator, Play install or debug build)
- **Must NOT fire:** any `android.root.*` or `.hook.*`. `PRIVILEGED_ACCESS` and `HOOK_FRAMEWORK`
  must be absent — the strong false-positive guards.
- **Situational:** `android.debug.*` fires when a debugger/ADB is attached (e.g. launching from
  Android Studio); `android.env.*` fires on an **emulator** (expected — the emulator *is* an
  emulator); `android.store.installer` fires for non-Play installs (sideloaded/debug); integrity
  signals fire only when their anchors (`expectedSigners`, `expectedApkSha256`,
  `expectedNativeLibSha256`) are configured. Validate the clean baseline on a physical Play install
  launched without a debugger.

### Compromised environment (expected positive detections)
| Scenario | Expect fired signals | Expect threat |
|---|---|---|
| Rooted (Magisk / KernelSU / Zygisk) | ≥1 of `android.root.*` | `PRIVILEGED_ACCESS` |
| `frida-server` / gadget / LSPosed / Xposed | ≥1 of `common.hook.frida_maps`, `android.hook.frida_port`, `android.hook.thread_name`, `android.hook.xposed` | `HOOK_FRAMEWORK` |
| Debugger / ADB attached | ≥1 of `android.debug.tracerpid`, `android.debug.ptrace` | `DEBUGGER` |
| Re-signed / repackaged APK (anchors configured) | `android.integrity.signature` and/or `android.integrity.apk_checksum` | `APP_INTEGRITY` |
| Tampered native core | `android.integrity.native_self_check` | `APP_INTEGRITY` (critical) |
