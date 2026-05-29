# Android Integrity Detection (mobshield-detect-integrity)

Kotlin-only v1 module for package signature, install source, APK checksum, and native library checksum verification. Signals map to `APP_INTEGRITY` and `UNOFFICIAL_STORE` (MASVS-RES-2).

## Signal catalog

| Signal | Technique | Default weight | Default confidence |
|--------|-----------|----------------|------------------|
| `android.integrity.signature` | APK Signing Scheme v2/v3 SHA-256 digests | 90 | 95 |
| `android.store.installer` | `getInstallSourceInfo()` installer package | 25 | 45 |
| `android.integrity.apk_checksum` | `PackageManager.requestChecksums` (API 31+) | 85 | 90 |
| `android.integrity.native_lib_checksum` | On-disk SHA-256 of `libmobshieldcore.so` | 80 | 85 |

`android.store.installer` is informational (`UNOFFICIAL_STORE`). Other integrity signals are high weight.

## Checks

### SignatureCheck.kt

`PackageManager.GET_SIGNING_CERTIFICATES` with `SigningInfo.apkContentsSigners`. Compares digests to `MobShieldConfig.expectedSigners`. Skipped when the allowlist is empty.

### InstallerOriginCheck.kt

Uses `PackageManager.getInstallSourceInfo()` (API 30+) and compares `installingPackageName` to `MobShieldConfig.allowedInstallSources` (defaults include Play Store IDs).

### ApkChecksumCheck.kt

On Android 12+, calls `requestChecksums` with `TYPE_WHOLE_SHA256` and compares to `MobShieldConfig.expectedApkSha256`.

### NativeLibChecksumCheck.kt

Opens `nativeLibraryDir/libmobshieldcore.so`, SHA-256 hashes the file, and compares to `MobShieldConfig.expectedNativeLibSha256`.

## Configuration

```kotlin
MobShieldConfig.builder()
    .expectedSigners(listOf("...64-char-sha256..."))
    .expectedApkSha256("...optional...")
    .expectedNativeLibSha256("...optional...")
    .allowedInstallSources(setOf("com.android.vending"))
    .build()
```

## Registration

```kotlin
IntegrityDetectionRegistrar.register(context, mobShieldConfig)
```

Module name: `integrity`. Module criticality: `100`.
