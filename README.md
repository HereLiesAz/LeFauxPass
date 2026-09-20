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

`GitHubUpdater` is a version-aware self-updater backed by GitHub Releases. It automatically checks at app launch (throttled to once every six hours) and can be forced manually from the top-right action.

The updater:

- reads the GitHub Releases API, including prereleases;
- selects the newest eligible APK asset;
- compares CI build numbers against the installed Android `versionCode`;
- verifies GitHub's SHA-256 asset digest when provided;
- verifies the downloaded APK package name and signing certificate;
- rejects same-version/downgrade APKs;
- downloads to app cache and hands the verified APK to Android's package installer;
- resumes installation after the user grants "Install unknown apps" permission when required.

Android still requires the user to approve the system package-installer prompt; LeFauxPass does not attempt privileged or silent installation.

The app therefore declares:

- `android.permission.INTERNET`
- `android.permission.REQUEST_INSTALL_PACKAGES`

## Build

From the repository root:

```sh
./gradlew assembleDebug
```

The Gradle daemon and CI are pinned to Temurin/JDK 21, matching the app's Java/Kotlin JVM target 21.

Dependency versions are centralized in:

```
gradle/libs.versions.toml
```

## CI and releases

`.github/workflows/build-apk.yml` builds and publishes debug APKs on successful pushes. It also creates a GitHub issue when the Gradle build fails.

Current workflow behavior:

- pushes: all branches are built;
- pull requests targeting `master` are built;
- rolling GitHub prereleases are published only from successful pushes to `master`;
- each rolling release keeps only the current APK asset plus optional build tools.

CI passes the repository commit count as Android `versionCode`, and the APK `versionName` is `major.minor.patch.build`. This gives the updater a monotonic version to compare.

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
