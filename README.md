# GlassTube

Android video client prototype built with Kotlin and Jetpack Compose.

## Current playback model

GlassTube uses YouTube's official embedded player and the YouTube Data API v3 for discovery/search. YouTube-served ads may still appear in the official player; this project does not claim to remove them. The app itself should not add third-party ad SDKs, banner ads, or promotional overlays.

## Build locally

1. Install Android Studio with JDK 17 and Android SDK 34.
2. Create `local.properties` in the project root and add `YT_API_KEY=your_key_here`.
3. Open the project in Android Studio and run the `app` configuration.

Never commit API keys. For GitHub Actions, add a repository secret named `YT_API_KEY`.

## Build APK with GitHub Actions

Open **Actions → Android CI → Run workflow**. The workflow builds a debug APK and uploads it as an artifact.

## Project layout

- `app/src/main/java/com/glasstube/YouTubeApi.kt`: API models and repository
- `AppViewModel.kt`: feed and player state
- `Glass.kt`: glass surfaces and navigation
- `Feed.kt`: video list and cards
- `Player.kt`: embedded player UI
- `MainActivity.kt`: app shell and tabs
