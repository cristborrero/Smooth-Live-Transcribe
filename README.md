# Smooth Live Transcribe

A production-quality Android app that turns your phone into a **smooth, teleprompter-style live transcription reader** — built for people learning English in real-world listening environments (churches, conferences, lectures).

## The Problem It Solves

Google Live Transcribe and similar apps cause text to **jump abruptly** when new words arrive. This breaks concentration, causes eye fatigue, and makes sustained reading painful. Smooth Live Transcribe treats the reading experience as a first-class feature.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                        │
│  TranscriptionScreen  SettingsScreen  SessionHistory  Detail     │
│                             │                                    │
│                     TranscriptionViewModel                        │
│                      SettingsViewModel                            │
│                  SessionHistoryViewModel                          │
└─────────────────────────────┬───────────────────────────────────┘
                               │ StateFlow / UI events
┌─────────────────────────────▼───────────────────────────────────┐
│                       Domain Layer                               │
│  ReadingEngine (anti-jump core)                                  │
│  TranscriptionRepository (interface)                             │
│  SessionRepository (interface)                                   │
└─────────────────────────────┬───────────────────────────────────┘
                               │ suspend fns / Flow
┌─────────────────────────────▼───────────────────────────────────┐
│                        Data Layer                                │
│  SpeechRecognizerWrapper (Android native STT)                    │
│  SpeechTranscriptionRepository                                   │
│  UserPreferencesRepository (DataStore)                           │
│  SessionRepositoryImpl + AppDatabase (Room)                      │
└─────────────────────────────────────────────────────────────────┘
```

**Why this architecture?**
- `ui → domain → data` dependency rule is strictly one-directional — the domain knows nothing about Android APIs
- ViewModels hold no Android context — purely testable Kotlin
- Repository interfaces in domain let us swap implementations without touching UI

## Tech Stack

| Category | Choice | Why |
|---|---|---|
| Language | Kotlin | Coroutines, sealed classes, extension fns |
| UI | Jetpack Compose + Material 3 | Declarative, animation-first, no XML |
| DI | Hilt | Compile-time verified, minimal boilerplate |
| State | Kotlin Coroutines + StateFlow | Structured concurrency, lifecycle-aware |
| Persistence | DataStore (prefs) + Room (sessions) | Type-safe, coroutine-native, no blocking I/O |
| Navigation | Navigation Compose | Type-safe routes, single NavController |
| STT | Android native SpeechRecognizer | No API key, works offline (on-device model), no privacy concerns |

## The Smooth Reading Engine

The `ReadingEngine` is the core innovation. It solves the jump problem with:

1. **Confirmed vs Partial separation** — confirmed text never changes; only the partial suffix updates
2. **Debouncing** (150ms) — partial results fire too fast for the eye to follow; we collapse rapid updates
3. **Append batching** — multiple final results arriving quickly are merged into one layout pass
4. **Spring-animated scroll** — `animateScrollTo` with `Spring.StiffnessLow` gives a natural deceleration, not a hard jump
5. **Manual scroll detection** — user dragging up pauses auto-follow; "Back to Live" resumes it

## Building the APK

### Prerequisites
- Android Studio Ladybug (2024.2+) or Android SDK command-line tools
- JDK 17+
- Android device or emulator running API 26+

### Debug APK (for testing)
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Install on device via ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (for distribution)
```bash
# 1. Create a keystore (one time):
keytool -genkey -v -keystore smooth-release.keystore -alias smooth -keyalg RSA -keysize 2048 -validity 10000

# 2. Configure signing in app/build.gradle.kts signingConfigs block

# 3. Build:
./gradlew assembleRelease
```

## Project Structure

```
app/src/main/java/com/levelone/smoothlivetranscribe/
├── core/ui/theme/          # Color, Type, Theme — Material 3
├── data/
│   ├── db/                 # Room: AppDatabase, SessionEntity, SessionDao, SessionRepositoryImpl
│   ├── preferences/        # DataStore: UserPreferences, UserPreferencesRepository
│   └── speech/             # SpeechRecognizerWrapper, SpeechTranscriptionRepository
├── di/                     # Hilt DI modules
├── domain/
│   ├── reading/            # ReadingEngine, ReadingState
│   ├── session/            # Session, SessionRepository (interface)
│   └── transcription/      # TranscriptionState, TranscriptionRepository (interface)
├── feature/
│   ├── history/            # SessionHistoryScreen/VM, SessionDetailScreen/VM
│   ├── settings/           # SettingsScreen, SettingsViewModel
│   └── transcription/      # TranscriptionScreen, TranscriptionViewModel
├── ui/
│   ├── components/         # ReadingContainer (shared composable)
│   └── navigation/         # AppNavGraph, Routes
├── MainActivity.kt
└── SmoothLiveTranscribeApp.kt
```

## UX Contract Compliance

| Requirement | Implementation |
|---|---|
| Text never jumps abruptly | ReadingEngine debounce + AnnotatedString append |
| Partial text visually distinct | Italic + lighter color + smaller size |
| Auto-scroll uses spring animation | `animateScrollTo` with `Spring.StiffnessLow` |
| Manual scroll pauses auto-follow | `detectVerticalDragGestures` + `pauseAutoFollow()` |
| "Back to Live" button | `AnimatedVisibility` FAB |
| Large font support | Configurable 14–36sp via DataStore |
| Keep screen awake | `view.keepScreenOn = true` during active session |

## Roadmap

- [ ] On-device STT with ML Kit (no network required)
- [ ] Multi-language support (auto-detect speaker language)
- [ ] Word-level confidence highlighting
- [ ] Export to PDF / Google Drive
- [ ] Widget for quick-start transcription
