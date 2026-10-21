# MobShield Android

Open-source mobile app hardening for Android: modular RASP detectors, signal aggregation, and per-build native personalization.

[![Android CI](https://github.com/inforaamitsolutions/MobShield-Android/actions/workflows/android-ci.yml/badge.svg)](https://github.com/inforaamitsolutions/MobShield-Android/actions/workflows/android-ci.yml)
[![License: Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
![minSdk: 24](https://img.shields.io/badge/minSdk-24-lightgrey.svg)

MobShield runs a set of detection modules, aggregates their weighted signals into `ThreatEvent`s, and reports your app's runtime posture through a listener — so your app can react to root, hooking, debugging, emulator, and integrity-tampering conditions.

## Modules

| Module | Purpose |
|--------|---------|
| `mobshield-core` | Public API facade, aggregator, validation harness, JNI bridge (skeleton) |
| `mobshield-detect-root` | Magisk, Zygisk, Shamiko, KernelSU signals |
| `mobshield-detect-hooks` | Frida, LSPosed, Xposed |
| `mobshield-detect-debugger` | ptrace, TracerPid |
| `mobshield-detect-environment` | Emulator, automation, ADB |
| `mobshield-detect-integrity` | Signature and build anchor |
| `mobshield-gradle-plugin` | Per-build personalization (`io.mobshield.personalize`) |
| `mobshield-sample-app` | Compose integration demo (all detectors) |

## Why MobShield

- **Fully auditable.** The detection logic is open source, so you can review it, fork it, and audit it. There's no binary blob.
- **No telemetry.** MobShield makes no network calls. Threat events go only to your listener and, if you choose, your backend.
- **Modular.** Add only the detectors you need.
- **Safe to roll out.** It's detect-only by default and never kills your process unless you opt in.
- **Harder to bypass generically.** The Gradle plugin personalizes each build, so one off-the-shelf bypass script doesn't fit every app.
- **Server-verifiable.** Signed posture reports let your backend decide whether to trust a session.

MobShield raises the cost of an attack. Like any client-side protection, it can't make an app unbreakable, so pair it with server-side checks.

## How it compares

| | MobShield | [freeRASP](https://github.com/talsec/Free-RASP-Android) | [RootBeer](https://github.com/scottyab/rootbeer) |
|---|---|---|---|
| License | Apache-2.0 (Kotlin); native core BSL | MIT wrapper + proprietary binary core, fair-usage policy | Apache-2.0 |
| Detection logic auditable | ✅ | ❌ (closed binary) | ✅ |
| Sends data to a vendor | ❌ No network calls | Incident data feeds Talsec's weekly Security Report | ❌ |
| Root (Magisk, Zygisk, Shamiko, KernelSU) | ✅ | ✅ | Basic (su binary, root/cloak apps, props) |
| Hooking (Frida, LSPosed, Xposed) | ✅ | ✅ | ❌ |
| Debugger | ✅ | ✅ | ❌ |
| Emulator / automation / ADB | ✅ | ✅ | ❌ |
| Signature / repackaging integrity | ✅ | ✅ | ❌ |
| Backend-verifiable report | ✅ Signed report, your own key | AppiCrypt (commercial plan) | ❌ |
| Per-build personalization | ✅ | ❌ | ❌ |
| Platforms | Android + [iOS](https://github.com/inforaamitsolutions/MobShield-iOS) | Android, iOS, Flutter, RN, Cordova, Capacitor | Android |

freeRASP is ahead on cross-platform framework support and a few extra checks, such as installation source and screen capture. If you need those today, it's a good choice. *Comparison based on each project's public README as of October 2026. Corrections are welcome via PR.*

## OWASP MASVS mapping

| MASVS v2 control | MobShield module |
|---|---|
| MASVS-RESILIENCE-1: platform integrity | `mobshield-detect-root`, `mobshield-detect-environment` |
| MASVS-RESILIENCE-2: anti-tampering | `mobshield-detect-integrity` |
| MASVS-RESILIENCE-4: anti-dynamic analysis | `mobshield-detect-hooks`, `mobshield-detect-debugger`, `mobshield-detect-environment` |

## Requirements

- JDK 17+
- Android SDK 34
- Gradle 8.7+ (wrapper included)
- NDK **28.0.13004108** (via SDK Manager; required for **16 KB page size** / Play compliance)

Native modules are built with `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON` and 16 KB ELF linker alignment. Rebuild and publish a new tag after upgrading the NDK.

## Install

Artifacts are published to **Maven Central** under the `io.mobshield` group. Add the personalization plugin, then core plus the detectors you want:

```kotlin
plugins {
    id("io.mobshield.personalize") version "1.0.7"
}

dependencies {
    val mobshield = "1.0.7"
    implementation("io.mobshield:mobshield-core:$mobshield")
    // optional detectors:
    implementation("io.mobshield:mobshield-detect-root:$mobshield")
    implementation("io.mobshield:mobshield-detect-hooks:$mobshield")
    implementation("io.mobshield:mobshield-detect-debugger:$mobshield")
    implementation("io.mobshield:mobshield-detect-environment:$mobshield")
    implementation("io.mobshield:mobshield-detect-integrity:$mobshield")
}
```

### JitPack (single umbrella dependency)

```kotlin
// settings.gradle.kts -> dependencyResolutionManagement.repositories
maven { url = uri("https://jitpack.io") }

// build.gradle.kts
dependencies {
    implementation("com.github.inforaamitsolutions:MobShield-Android:mobshield:v1.0.7")
}
```

The umbrella artifact pulls in core and all detect modules transitively.

## Quick start

Register the detectors, implement a listener, and start MobShield with an application `Context`. It runs in **detect-only** mode by default (it reports; it never terminates your process).

```kotlin
import io.mobshield.core.MobShield
import io.mobshield.core.MobShieldConfig
import io.mobshield.core.MobShieldListener
import io.mobshield.core.ThreatEvent
import io.mobshield.detect.root.RootDetectionRegistrar
import io.mobshield.detect.hooks.HookDetectionRegistrar
import io.mobshield.detect.debugger.DebugDetectionRegistrar
import io.mobshield.detect.environment.EmulatorDetectionRegistrar
import io.mobshield.detect.integrity.IntegrityDetectionRegistrar

val listener = object : MobShieldListener {
    override fun onThreat(event: ThreatEvent) {
        Log.w("MobShield", "threat: ${event.type} ${event.severity} score=${event.score}")
    }

    override fun onAllChecksFinished(events: List<ThreatEvent>) {
        Log.i("MobShield", "scan finished: ${events.size} threat(s)")
    }
}

val config = MobShieldConfig() // detect-only by default
RootDetectionRegistrar.register(context, config)
HookDetectionRegistrar.register(context, config)
DebugDetectionRegistrar.register(context, config)
EmulatorDetectionRegistrar.register(context, config)
IntegrityDetectionRegistrar.register(context, config)
MobShield.start(context, config, listener)
```

Read the current posture at any time, or stop scanning:

```kotlin
val state = MobShield.getState() // riskLevel, activeThreats, running, lastScanMs
MobShield.stop()
```

Common configuration (all optional):

```kotlin
val config = MobShieldConfig.builder()
    .periodicIntervalSec(30)                       // rescan every 30s (default: one scan at start)
    .expectedPackageId("com.example.app")          // anchor package-id integrity
    .expectedSigners(listOf("<sha256-hex>"))       // anchor signing-cert integrity
    .detectOnly(false)                             // opt in to termination
    .terminationPolicy(TerminationPolicy.EXIT_ON_CRITICAL)
    .build()
```

For a signed posture report you can verify on your backend, see `MobShield.currentSignedReport(key)`.

## Build

```bash
./gradlew :mobshield-sample-app:assembleDebug
./gradlew test
```

See [mobshield-sample-app/README.md](mobshield-sample-app/README.md) for a walkthrough.

## Documentation

- [Detection validation matrix](docs/validation.md) — every signal each module emits and the clean vs compromised baselines.
- [Detection notes](docs/detection) — per-detector background.

## Release

Publishing a `v*.*.*` tag builds and signs the artifacts and publishes them to Maven Central. Publishing requires these repository secrets: `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.

Local verification:

```bash
./gradlew :mobshield-core:publishToMavenLocal -PVERSION_NAME=1.0.7-SNAPSHOT
```

## License

- Kotlin and Gradle sources: [Apache-2.0](LICENSE)
- Native core (when implemented): [LICENSE-BSL](LICENSE-BSL), Change Date 2028-05-25
