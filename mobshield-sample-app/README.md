# MobShield Android sample app

Integration reference for `mobshield-sample-app`: Compose UI, all detection modules, Gradle personalization, and Timber logging.

## Prerequisites

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34

## Run in under 15 minutes

1. Clone the MobShield repository and open `mobshield-android/` in Android Studio.
2. Sync Gradle. The composite `mobshield-gradle-plugin` is included via `includeBuild`.
3. Select the `mobshield-sample-app` run configuration.
4. Run on an emulator or device (API 24+).
5. Tap **Start** on the Threats tab. Watch Logcat filtered by `MobShieldSample` for Timber output.
6. Open **Signals** and tap **Refresh signals** to inspect raw probe output.
7. Optional: `./gradlew :mobshield-sample-app:mobshieldInfo` to print personalization metadata.

## Gradle personalization

See the marked block in `build.gradle.kts`:

```kotlin
mobshield {
    expectedSigningCertSha256 = "..." // your debug/release cert SHA-256
    expectedPackageName = "io.mobshield.sampleapp"
    expectedInstallers = listOf("com.android.vending")
    aggressive = false
}
```

Replace the placeholder cert (all zeros) before running `:mobshieldVerify` on release builds.

## Modules demonstrated

- `mobshield-core` with default `SignalAggregator` thresholds
- `mobshield-detect-root`, `-hooks`, `-debugger`, `-environment`, `-integrity`
- `io.mobshield.personalize` Gradle plugin

## Screens

| Tab | Purpose |
|-----|---------|
| Threats | Live `ThreatEvent` cards with severity and score |
| Config | Detect-only vs fail-closed, per-module toggles |
| Signals | All raw `Signal` rows from enabled modules |
| About | SDK version, build entropy preview, MASVS table |
