# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

- **Build APK:** `./gradlew assembleDebug` (for debug APK) or `./gradlew assembleRelease` (for release APK)
- **Install on connected device:** `./gradlew installDebug`
- **Run all unit tests:** `./gradlew testDebugUnitTest`
- **Run a single test:** `./gradlew testDebugUnitTest --tests "com.alfan.fanllymusic.util.LrcParserTest"`
- **Lint check:** `./gradlew lintDebug`
- **Clean build:** `./gradlew clean`

## Architecture & Structure

This is an Android music player app built entirely with **Kotlin**, **Jetpack Compose** for UI, and **Media3 (ExoPlayer)** for playback. The architecture follows modern Android recommendations (MVVM, Clean Architecture concepts) with **Hilt** for Dependency Injection.

### Core Components
1. **App & DI (`/di`)**: `FanllyMusicApp.kt` is the application class. `AppModule.kt` provisions the Room database (DAOs for songs, playlists, history, lyrics, artwork), OkHttpClient, and Retrofit (Lrclib API).
2. **Media Playback (`/media`)**:
   - `FanllyMusicService`: A `MediaSessionService` that manages the `ExoPlayer` instance and binds to Android's media framework.
   - `FanllyMediaController`: A singleton wrapper around `MediaController` that exposes StateFlows (`isPlaying`, `currentSongId`, `positionMs`, `queueState`) for the UI to consume. This bridges UI/ViewModels to the playback service.
3. **UI & Navigation (`/ui`)**:
   - **Navigation:** Handled by `FanllyNavGraph.kt` utilizing Compose Navigation. Includes nested/bottom navigation (`Library`, `Search`, `Playlists`, `Settings`) and a persistent `MiniPlayer` / `GlassNavBar` that can expand into the full `NowPlayingScreen`.
   - **Components:** Modular Compose components exist in `/ui/components` (e.g., `SongCard`, `MiniPlayer`, `GlassNavBar`, `LyricsView`).
   - **State Management:** Screens use ViewModels (e.g., `LibraryViewModel`, `NowPlayingViewModel`) utilizing `StateFlow` and injected via `@HiltViewModel`.
4. **Data Layer (`/data`)**:
   - **Local Storage (`/local`)**: Uses Room. `FanllyDatabase` orchestrates entities like `SongEntity`, `PlaylistEntity`, `LyricsCacheEntity`, and `ArtworkColorEntity`.
   - **Remote (`/remote`)**: `LrclibApi` for fetching remote lyrics.
   - **Repositories (`/repository`)**: Repositories abstract local DB interactions and remote fetches.
5. **Domain & Utils (`/domain`, `/util`)**:
   - Models include `Song`, `Playlist`, `QueueState`, `LyricLine`.
   - Utils cover specific functionality like `MediaStoreHelper` (scanning local audio), `LrcParser` (parsing LRC lyric files), `AudioQualityDetector`, and local/embedded lyric reading.

### Important Notes
- Minimum SDK is 26, Target SDK is 36.
- The project resolves conflicts in packaging by picking firsts/excluding resources in `app/build.gradle.kts`. When adding libraries with meta-inf conflicts, check the `packaging { resources { ... } }` block.
- Permissions: It handles Android 13+ audio permissions natively (`Manifest.permission.READ_MEDIA_AUDIO` vs `READ_EXTERNAL_STORAGE`).