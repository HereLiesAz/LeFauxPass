# LeFauxPass

LeFauxPass is an unofficial Android UI prototype for a transit-ticket-style screen. It is not affiliated with, endorsed by, or an official product of the New Orleans Regional Transit Authority.

## Current implementation

The canonical app is the native Android project at the repository root:

- **Application ID:** `com.hereliesaz.lefauxpass`
- **UI:** Kotlin + Jetpack Compose
- **Minimum Android:** API 26
- **Compile / target SDK:** 37
- **Media playback:** AndroidX Media3 / ExoPlayer
- **Default branch:** `master`

`LeFauxPassRN/` is an alternate React Native prototype. `rta-ticket-clone/` is a historical web implementation. Neither is built by the root Android release workflow.

## Animation

The native app's live animation is:

```
app/src/main/res/raw/animation.mp4
```

`MainActivity.kt` loads it as `R.raw.animation` with Media3, starts playback automatically, and loops it continuously without player controls. The replacement MP4 currently in `master` is the source used by native Android builds.

The similarly named WebP assets under `LeFauxPassRN/` and `rta-ticket-clone/` belong to those separate implementations and do not control the native Android app.

## Ticket state

The expiration timestamp is stored locally in `SharedPreferences` by `ExpirationManager`. When no valid stored timestamp exists, the app creates a new expiration time 1 hour 56 minutes from the current time.

## Updates

`GitHubUpdater` checks the public GitHub **latest release** endpoint, downloads the first APK asset it finds to app cache, and opens Android's package installer through a `FileProvider`.

The app therefore declares:

- `android.permission.INTERNET`
- `android.permission.REQUEST_INSTALL_PACKAGES`

Note that GitHub's `releases/latest` endpoint refers to the latest non-prerelease release. The CI workflow also publishes rolling prerelease builds, so the in-app updater and the prerelease channel are not necessarily the same release.

## Build

From the repository root:

```sh
./gradlew assembleDebug
```

The Gradle daemon is pinned to JDK 17 in `gradle/gradle-daemon-jvm.properties`. The Android source is configured for Java/Kotlin JVM target 21.

Dependency versions are centralized in:

```
gradle/libs.versions.toml
```

## CI and releases

`.github/workflows/build-apk.yml` builds and publishes debug APKs on successful pushes. It also creates a GitHub issue when the Gradle build fails.

Current workflow behavior:

- pushes: all branches
- pull requests: `main`
- repository default branch: `master`

That branch-name mismatch is intentional documentation of the current workflow state, not a claim that PR builds against `master` are enabled.

## Repository map

```
app/                 Native Android app; canonical implementation
gradle/              Gradle wrapper, daemon, and version catalog
LeFauxPassRN/        Alternate React Native prototype
rta-ticket-clone/    Historical web/Jekyll prototype
.github/workflows/   CI and release automation
version.properties   App version inputs
AGENTS.md            Repository guidance for coding agents
```

## Privacy

The bundled privacy policy is at `app/src/main/assets/privacy_policy.txt`. The app has no account system, ads, analytics SDK, or project-operated backend in the current source, but it does make GitHub network requests for update checks and APK downloads.

_Last documentation update: September 20, 2026._
