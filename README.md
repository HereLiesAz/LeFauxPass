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

Android and the PWA draw the ticket animation in the shared Compose `TicketAnimation` canvas. Its radius and duration table follows each frame of `LeFauxPassRN/assets/animation.webp`. Edit `TicketAnimationColors` to change the default ring, paper, background, or shadow. The inset is shaded from the selected ring color. The static logo is cropped from the reference.

The hamburger menu on My tickets opens a color wheel, with brightness and hex input for choosing the ring color. The choice and the one-time tip shown on the first visit to My tickets are stored in Android preferences or the browser’s local storage. Version `3.0` defaults to the reference pink (`#C57A98`) when no color is saved. Existing preset selections carry over. When changing a shipped palette color, increment `versionMajor` in `version.properties`, reset minor and patch to zero, and tag the resulting commit with the new `MAJOR.0` version.

“Need more?” below the wheel opens an email draft for suggesting another ring color. The link beneath it opens the GitHub repository.

The UI is Compose Multiplatform (`shared/`), built both as the Android app (`app/`) and as a PWA deployed to GitHub Pages (`./gradlew :shared:wasmJsBrowserDistribution`; workflow `deploy-pwa.yml`). The app opens on a dark "Ticket Details" screen (`TicketDetailsScreen.kt`). "Activate ticket" raises a confirmation bottom sheet; "Activate" moves to the animated ticket screen. Back from the ticket screen opens "My tickets" (`MyTicketsScreen.kt`); tapping the green active-ride card returns to the animation. Navigation state is held for the session only. On the web, expiration is stored in localStorage and the browser back button maps to in-app back.

The similarly named WebP assets under `LeFauxPassRN/` and `rta-ticket-clone/` belong to those separate implementations and do not control the native Android app.

## Ticket state

The expiration timestamp is stored locally in `SharedPreferences` by `ExpirationManager`. When no valid stored timestamp exists, the app creates a new expiration time 1 hour 56 minutes from the current time.

## Updates

`GitHubUpdater` is a version-aware self-updater backed by GitHub Releases. It automatically checks when the animated ticket screen opens (throttled to once every six hours) and can be forced manually from the top-right action.

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

`.github/workflows/build-apk.yml` builds debug APKs and publishes them from version-tag pushes. It also creates a GitHub issue when the Gradle build fails.

Current workflow behavior:

- pushes: all branches are built;
- pull requests targeting `master` are built;
- pushes of version tags beginning with a digit or with `v` followed by a digit build the tagged commit and publish a GitHub prerelease with that exact tag as its title;
- each new prerelease contains the debug APK and build-tools archive. Publishing skips tags that already have a published release. An existing draft stops publication for review.

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
