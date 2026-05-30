# FanllyMusic - OpenCode Agents Guide

FanllyMusic is a local lossless Android music player designed for a smooth, premium Apple Music-inspired experience.

## Tech Stack & Architecture
- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose, Material 3
- **Playback**: Media3 / ExoPlayer
- **Database**: Room
- **DI**: Hilt
- **SDK**: Min 26, Target 36

## Important Working Rules
1. **No Hallucination**: Do not guess APIs, imports, or classes. Read existing code first.
2. **No Unprompted Upgrades**: Do not upgrade Gradle, Kotlin, Compose, or AGP unless explicitly requested.
3. **Pure Compose**: Prefer Jetpack Compose over XML layouts.
4. **Build Safety**: Always fix compile errors before continuing. Make small, safe changes in phases.
5. **AAPT2 Requirement**: Keep this exact AAPT2 override if editing `gradle.properties`:
   `android.aapt2FromMavenOverride=/data/data/com.itsaky.androidide/files/home/android-sdk/build-tools/35.0.0/aapt2`
6. **No Feature Removal**: Do not remove existing features or rewrite the whole project without permission.

## Development Commands
- **Build APK (Debug)**: `./gradlew assembleDebug`
- **Build APK (Release)**: `./gradlew assembleRelease`
- **Test**: `./gradlew testDebugUnitTest`
- **Lint**: `./gradlew lintDebug`

## Performance Priorities
- Keep the Main thread clean. Use `Dispatchers.IO` for file scanning, lyrics parsing, DB, and network work.
- Prevent white flashes during navigation and frozen first launches.
- Keep Composables light. Use `LazyColumn`/`LazyGrid` keys. Avoid duplicated `LaunchedEffect` loops.
- Use stable UI state. Clamp indices safely for progress and lyrics.

## UI / UX Priorities
- **Style**: Premium dark mode, large readable typography, elegant spacing.
- **Now Playing**: Immersive, large centered artwork, clear text, responsive controls, playback menu in bottom sheet.
- **Lyrics Mode**: Full-screen feel, auto-scrolling synced LRC. Active line bright/bold near the center; inactive lines dimmed. Auto-scroll only changes on active index shift. Do not parse lyrics repeatedly inside Composables.
- **Bottom Navigation**: Floating glass style with a smooth selected indicator.

## Data & Library
- **Scanner**: Keep it lightweight. Scan local FLAC/lossless files safely. Avoid rescanning on every tab switch.
- **Library State**: Cache library state in ViewModel or Repository. Use filename as a fallback if metadata is wrong.