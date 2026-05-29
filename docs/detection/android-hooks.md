# Android Hook Framework Detection (mobshield-detect-hooks)

Detects Frida, objection, LSPosed, Xposed, and related instrumentation. Module name: `hooks`. Criticality: 100.

## Signal catalog

| Signal | Technique | Default weight | Default confidence |
|--------|-----------|----------------|------------------|
| `common.hook.frida_maps` | `/proc/self/maps` scan | 75 | 85 |
| `common.hook.prologue` | libc prologue baseline + trampoline heuristic | 80 | 90 |
| `android.hook.frida_port` | TCP connect to 127.0.0.1:27042/27043 (100ms) | 85 | 95 |
| `android.hook.thread_name` | `/proc/self/task/*/comm` Frida threads | 70 | 80 |
| `android.hook.art_dex` | JNI class load probe for Xposed/LSPosed | 75 | 85 |
| `android.hook.suspicious_library` | maps vs `System.mapLibraryName` | 55 | 65 |
| `android.hook.xposed` | `ClassLoader.loadClass` for bridge types | 70 | 80 |
| `android.hook.stack_leak` | `Thread.getAllStackTraces()` prefix scan | 65 | 75 |

Tune via `MobShieldConfig.detectionTuning`.

## Native checks (`libmobshieldhooks.so`)

### 1. proc_maps_scan.cpp

Reads `/proc/self/maps` for `frida-agent`, `gum-js-loop`, `linjector`, `XposedBridge.jar`, `/data/adb/lspd`, and related strings.

**Survives:** Frida hide that keeps artifacts in memory maps.

**Bypass:** manual map cleaning, renamed agents.

**Restricted /proc:** returns `UNAVAILABLE` (no signal).

### 2. function_prologue_inspect.cpp

Reads first 16 bytes of `open`, `read`, `syscall`, `android_dlopen_ext` via `dlsym`. Compares against XOR-encrypted baselines keyed by `MOBSHIELD_HOOKS_ENTROPY_SEED_*` from `generateHooksBuildInfo` (overridden by `mobshield-plugin` at app build time). When baselines are unset (`0xFF`), uses ARM64 trampoline heuristic only.

**Survives Frida Stalker:** yes for inline hooks that patch function entry (Stalker does not rewrite prologue in all modes).

**Bypass:** hardware breakpoints only, late inline hooks after baseline capture, ARM64 hook gadgets that preserve first 16 bytes.

### 3. frida_port_probe.cpp

Non-blocking `connect()` to loopback ports 27042 and 27043 with 100ms socket timeout.

**Survives:** network-only hide if server still listens locally.

**Bypass:** custom port, host-only Frida.

### 4. thread_name_scan.cpp

Scans `/proc/self/task/*/comm` for `gmain`, `gdbus`, `gum-js-loop`, `pool-frida`, `linjector`.

**Survives:** renamed threads still often match `gum-js-loop` unless patched.

**Bypass:** thread name spoofing.

### 5. art_inspection.cpp

JNI-only: uses app `ClassLoader.loadClass` for known bridge class names. No private ART struct walking.

**Survives:** Xposed/LSPosed when classes are loadable.

**Bypass:** Shamiko package/class hide; customized package names.

## Kotlin checks

### 6. SuspiciousLibraryScan.kt

Maps file must contain suspicious library lines (frida/xposed/lsposed tokens), not merely `libc.so`.

### 7. XposedModuleQuery.kt

Try/catch `Class.forName` via app `ClassLoader` (no shell).

### 8. ClassNameLeakScan.kt

Inspects stack traces for `de.robv.android.xposed`, `org.lsposed`, `frida`, etc.

## Integration

```kotlin
HookDetectionRegistrar.register(context, config)
MobShield.start(context, config, listener)
```

## Constraints

- No `Runtime.exec` or shell.
- Native JNI calls use 100ms Kotlin `withTimeout` per check.
- Graceful degradation when `/proc` is unavailable (Android 11+ scoped restrictions).
- Encrypted prologue baselines tied to per-build entropy seed.

## Emulator acceptance

Stock Pixel emulator API 34/35 without `frida-server` should produce zero `HOOK_FRAMEWORK` events at CRITICAL severity after aggregation.
