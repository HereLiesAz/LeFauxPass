# LeFauxPass React Native prototype

This directory contains an **alternate React Native implementation** of LeFauxPass. It is retained for reference and experimentation; it is **not** the canonical Android app and is not built by the repository's root Gradle/CI release path.

For the current native Android implementation, use the repository root `:app` module.

## Stack

- React Native 0.82.1
- React 19.1.1
- TypeScript
- AsyncStorage
- Node.js 20+

The prototype stores its ticket expiration in AsyncStorage under `ticketExpirationTime` and updates the displayed clock once per second.

## Animation

This prototype currently renders:

```
LeFauxPassRN/assets/animation.webp
```

That asset is independent of the native Android animation at:

```
app/src/main/res/raw/animation.mp4
```

Changing this WebP does **not** change the animation in native Android releases.

## Install

From `LeFauxPassRN/`:

```sh
npm install
```

Start Metro:

```sh
npm start
```

Run Android:

```sh
npm run android
```

Run tests:

```sh
npm test
```

Run lint:

```sh
npm run lint
```

## iOS

The project includes iOS scaffolding. On macOS with the required Apple toolchain:

```sh
bundle install
bundle exec pod install
npm run ios
```

## Maintenance status

Treat this directory as a secondary prototype unless a task explicitly targets React Native. Native Android fixes, release changes, updater changes, and animation replacements should normally be made in the root Android project instead.

_Last updated: September 20, 2026._
