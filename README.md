# GlassTube

GlassTube is an Android video discovery and playback client built with Kotlin and Jetpack Compose. The interface uses a teal, layered Liquid Glass-inspired material system, with light and dark appearances following Android's system theme.

## What works in this foundation

- Popular-video discovery for India through YouTube Data API v3.
- Video search through YouTube Data API v3.
- Official YouTube embedded playback in an in-app player screen.
- A device-local saved-video library.
- Reusable glass surfaces, search fields, chips, cards, and floating navigation.
- GitHub Actions build that produces a debug APK artifact.

## Playback and YouTube requirements

GlassTube embeds videos using YouTube's official player. YouTube-served ads, branding, and player controls may appear. GlassTube does not add third-party ad SDKs, banner ads, promotional overlays, video downloading, or offline playback.

The YouTube Data API has a quota. Search requests consume more quota than popular-video requests, so avoid repeatedly submitting the same search.

## Add the YouTube API key

1. Create or select a Google Cloud project.
2. Enable YouTube Data API v3 and create an API key.
3. Restrict the key to the YouTube Data API v3 and, where practical, to the Android app package `com.glasstube` and its signing certificate.
4. For local builds, create a root-level `local.properties` file containing:

```properties
YT_API_KEY=your_key_here
```

5. For GitHub Actions builds, add a repository secret named `YT_API_KEY`.

Never commit `local.properties` or a raw API key. Client-side API keys are still extractable from an APK, so use Google Cloud API and application restrictions; a GitHub secret does not make a key embedded in a distributed APK private.

The app can compile without a key, but video discovery and search remain unavailable until one is configured.

## Build

- Android Studio with Android SDK 36
- JDK 17
- Gradle 8.13
- Minimum Android version: Android 8.0 (API 26)

Open the repository in Android Studio and sync the Gradle project. From a shell with Gradle 8.13 installed, build with:

```bash
gradle --no-daemon assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

The **Android CI** workflow runs on pushes to `main`, pull requests, and manual dispatch. It builds the debug APK and uploads it as the `GlassTube-debug-apk` artifact.

## Design note

Apple's Liquid Glass renderer is part of Apple's platforms and is not available as an Android library. GlassTube recreates the visual direction with native Compose layers, translucent gradients, light-catching edges, rounded surfaces, and adaptive contrast. It is not Apple's proprietary implementation.
