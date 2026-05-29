# MobShield Android

Open-source mobile app hardening for Android: modular RASP detectors, signal aggregation, and per-build native personalization.

**Specification:** [mobshield-spec/MOBSHIELD_SPEC.md](../mobshield-spec/MOBSHIELD_SPEC.md)

## Modules

| Module | Purpose |
|--------|---------|
| `mobshield-core` | Public API facade, aggregator, JNI bridge (skeleton) |
| `mobshield-detect-root` | Magisk, Zygisk, Shamiko, KernelSU signals |
| `mobshield-detect-hooks` | Frida, LSPosed, Xposed |
| `mobshield-detect-debugger` | ptrace, TracerPid |
| `mobshield-detect-environment` | Emulator, automation, ADB |
| `mobshield-detect-integrity` | Signature and build anchor |
| `mobshield-plugin` | Legacy plugin alias for personalize |
| `mobshield-gradle-plugin` | Per-build personalization (`io.mobshield.personalize`) |
| `mobshield-sample-app` | Compose integration demo (all detectors) |

## Requirements

- JDK 17+
- Android SDK 34
- Gradle 8.7+ (wrapper included)

## Build

```bash
./gradlew :mobshield-sample-app:assembleDebug
./gradlew test
```

See [mobshield-sample-app/README.md](mobshield-sample-app/README.md) for a 15-minute walkthrough.

## Install

See [docs/install.md](../docs/install.md) for Maven Central coordinates (Kotlin and Groovy DSL).

### JitPack (single dependency)

Push this directory as its own GitHub repo (or use it as the repo root), tag a release, then in your app:

```kotlin
dependencies {
    implementation("com.github.YOUR_GITHUB_USER:MobShield-Android:mobshield:v0.1.0")
}
```

This umbrella artifact pulls in core and all detect modules transitively.


Local verification:

```bash
./gradlew :mobshield-core:publishToMavenLocal -PVERSION_NAME=0.1.0-SNAPSHOT
```

## Release

Tag `v*.*.*` triggers [.github/workflows/release.yml](../.github/workflows/release.yml): tests, AAR reproducibility check, signed publish to Maven Central, and `mobshield-spec/signals.json` on the GitHub Release.

Required repository secrets: `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.

## License

- Kotlin and Gradle sources: [Apache-2.0](LICENSE)
- Native core (when implemented): [LICENSE-BSL](LICENSE-BSL), Change Date 2028-05-25
