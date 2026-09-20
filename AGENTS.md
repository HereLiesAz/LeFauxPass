# LeFauxPass Agent Guide

This file is the working guide for automated coding agents operating on **HereLiesAz/LeFauxPass**. Ignore older assumptions about unrelated projects; this repository is LeFauxPass.

## Source of truth

The canonical implementation is the native Android project at the repository root:

- module: `:app`
- package / application ID: `com.hereliesaz.lefauxpass`
- UI: Kotlin + Jetpack Compose
- default branch: `master`
- min SDK: 26
- compile / target SDK: 37
- dependency catalog: `gradle/libs.versions.toml`

`LeFauxPassRN/` and `rta-ticket-clone/` are alternate/historical implementations. Do not modify them as a substitute for changing the native Android app unless the task explicitly names them.

## Current native behavior

`MainActivity.kt` renders the ticket screen, live clock, expiration information, and the looping animation.

The production animation asset is:

```
app/src/main/res/raw/animation.mp4
```

It is referenced as `R.raw.animation` and played through Media3 / ExoPlayer. Replacing a WebP in either legacy implementation does not replace the native animation.

`ExpirationManager.kt` persists the expiration timestamp locally in `SharedPreferences`.

`GitHubUpdater.kt` checks the GitHub Releases collection, including prereleases. Automatic checks are throttled to six hours; manual checks bypass the throttle. It selects the newest eligible APK, verifies GitHub's SHA-256 digest when available, validates package ID and signer, rejects non-upgrades, and then launches Android's package installer through `FileProvider`.

The updater may direct the user to Android's "Install unknown apps" settings when permission is not yet granted, then resumes the pending install when the activity returns.

## Build

Use the root Gradle wrapper:

```sh
./gradlew assembleDebug
```

Useful verification:

```sh
./gradlew testDebugUnitTest assembleDebug
```

The Gradle daemon and CI use JDK 21. Android compile options and the Kotlin JVM target are also 21.

Do not regenerate a vendor-specific daemon toolchain file unless required. A previous JetBrains JDK 21 auto-provisioning configuration caused CI failures because the provisioned toolchain did not satisfy Gradle's required JDK executables.

## CI / release behavior

`.github/workflows/build-apk.yml` currently:

1. checks out the repository,
2. injects optional service configuration,
3. generates the signing keystore from repository secrets,
4. sets up Temurin JDK 21,
5. runs `assembleDebug` with the commit count supplied as `versionBuild`,
6. creates a GitHub issue on build failure,
7. publishes/updates the rolling prerelease only for successful pushes to `master`.

Pull requests targeting `master` are built but do not publish releases. The rolling release deletes stale APK assets before uploading the new one. The in-app updater intentionally consumes prereleases, so this rolling release is the active update channel.

## Versioning

`version.properties` is the root version input. At this documentation revision it contains:

```
versionMajor=1
versionMinor=2
versionPatch=0
versionBuild=6
```

The Android Gradle configuration uses CI's `versionBuild` as `versionCode` and produces `versionName` as `major.minor.patch.build`. CI uses the same four-part version in APK asset names.

## Change discipline

- Prefer one coherent commit for one requested change set.
- Preserve existing behavior unless the user asks to alter it.
- Verify the actual referenced asset or code path before replacing media.
- Do not infer that a published APK matches `master`; inspect the release target commit when that distinction matters.
- Keep documentation synchronized with implementation changes.
- Never put credentials, signing material, API keys, or generated keystores into source control.
- Avoid editing generated or unrelated legacy files simply because they have similar names.

## Documentation

Repository-facing documentation consists of:

- `README.md` — project overview and current architecture
- `AGENTS.md` — automation/development guidance
- `LeFauxPassRN/README.md` — status and setup of the alternate RN prototype
- `app/src/main/assets/privacy_policy.txt` — privacy disclosure bundled with the Android app

When behavior affecting networking, storage, permissions, releases, or the animation changes, update the relevant documentation in the same change set.

_Last updated: September 20, 2026._
