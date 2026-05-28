# Smooth Live Transcribe — Project Status

## Phase 1 — DOC 01: Project Foundation ✅
**Status:** Complete

### What was implemented
- Full Android project scaffold (Gradle Kotlin DSL, version catalog `libs.versions.toml`)
- Package structure: `core`, `data`, `domain`, `feature`, `ui` with clean separation
- Material 3 theme with dark/light modes + high-contrast accessibility option
- Navigation Compose with 4 routes: Transcription, Settings, SessionHistory, SessionDetail
- Hilt DI configured (Application class + DatabaseModule + RepositoryModule)
- Domain interfaces defined (TranscriptionRepository, SessionRepository)
- README with architecture diagram and build instructions

### Technical decisions
| Decision | Rationale |
|---|---|
| Single `app` module | Appropriate scale — avoids multi-module overhead for a single-feature app |
| Compose-only (no XML) | Animation-first development; XML + Compose hybrid would complicate ReadingContainer |
| Navigation Compose | No Fragment backstack management; all state in ViewModels |
| Hilt over Koin | Compile-time verification catches DI errors before runtime |
| KSP over KAPT | 2x faster incremental builds; required for Hilt + Room in Kotlin 2.0 |

### Known issues / Tech debt
- No app icon (placeholder vector only) — to be addressed in DOC 06

---

## Phase 2 — DOC 02: Audio Permissions & SpeechRecognizer ✅
**Status:** Complete

### What was implemented
- `SpeechRecognizerWrapper` with `callbackFlow` — bridges RecognitionListener to Flow
- Auto-restart on recoverable errors (ERROR_NO_MATCH, ERROR_SPEECH_TIMEOUT, etc.)
- Full error mapping (14 error codes) to human-readable messages with recoverability flag
- `TranscriptionState` sealed class with 7 states covering full recognition lifecycle
- `SpeechTranscriptionRepository` mapping SpeechEvents → TranscriptionState
- TranscriptionScreen: mic permission flow with `rememberLauncherForActivityResult`
- Status bar showing current recognition state with pulsing dot animation

### Technical decisions
| Decision | Rationale |
|---|---|
| `callbackFlow` for SpeechRecognizer | Bridges callback-based API to coroutine world cleanly; cleanup via `awaitClose` |
| Auto-restart after FinalResult | SpeechRecognizer stops after each segment; restart is required for continuous sessions |
| Separate SpeechEvent and TranscriptionState | `SpeechEvent` = raw API events; `TranscriptionState` = domain intent. Enables testing domain logic without Android |
| EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS = 5000 | Prevents premature session termination during natural pauses in speech |

### Known issues
- SpeechRecognizer requires network (Google Speech API) — offline STT is a future roadmap item

---

## Phase 3 — DOC 03: Smooth Reading Engine ✅
**Status:** Complete

### What was implemented
- `ReadingEngine` — domain-layer stateful engine managing confirmedText + partialText
- **Anti-jump strategy:**
  - Debounce (150ms) on partial results — collapses rapid recognizer events
  - AnnotatedString with stable confirmed prefix + mutable partial suffix
  - Scroll version counter (not pixel offset) — decouples layout from scroll logic
  - Spring animation (`Spring.StiffnessLow`, `DampingRatioNoBouncy`) for smooth deceleration
- `ReadingContainer` composable:
  - `rememberScrollState` + `animateScrollTo` in `LaunchedEffect`
  - Manual scroll detection via `detectVerticalDragGestures`
  - `onUserScrolled()` callback → `ReadingEngine.pauseAutoFollow()`
- "Back to Live" ExtendedFAB with `AnimatedVisibility` (slide + fade)
- Partial text rendered: italic + lighter color + 5% smaller font
- Last confirmed chunk highlighted with `Primary.copy(alpha=0.25f)` background for 800ms

### Technical decisions
| Decision | Rationale |
|---|---|
| ScrollVersion counter vs pixel offset | Compose measures layout asynchronously; using maxValue at animation time is more reliable than pre-computing offsets |
| 150ms debounce | Below human perception threshold for "instant" feedback; eliminates layout thrashing from 10-30 partial events/second |
| Spring.StiffnessLow | Smooth deceleration feels like physical inertia — prevents the jarring "snap" of linear animations |
| `detectVerticalDragGestures` instead of `NestedScrollConnection` | Simpler implementation; avoids nested scroll conflicts with `verticalScroll` modifier |

---

## Phase 4 — DOC 04: Reading Settings & Accessibility ✅
**Status:** Complete

### What was implemented
- `UserPreferences` data class with 8 configurable settings
- `UserPreferencesRepository` with DataStore — atomic writes, coroutine-native
- `SettingsScreen`:
  - Live preview card showing font + line height changes instantly
  - Sliders for font size (14–36sp) and line height (×1.2–×2.0)
  - Toggles: dark theme, high contrast, show partial, highlight last chunk, keep screen on
  - Scroll speed multiplier (0.5×–2.0×)
- Settings applied in real-time to TranscriptionScreen via StateFlow
- `view.keepScreenOn` activated during active listening sessions

### Defaults calibrated for:
- Church/conference environment: 22sp font, ×1.6 line height, dark theme, keep screen on

---

## Phase 5 — DOC 05: Session History & Export ✅
**Status:** Complete

### What was implemented
- Room database (`AppDatabase`) with `SessionEntity` and `SessionDao`
- `SessionRepository` interface + `SessionRepositoryImpl`
- `SessionHistoryScreen`: LazyColumn of session cards, swipe-delete, empty state
- `SessionDetailScreen`: editable title, full text view, share/export via `Intent.ACTION_SEND`
- Auto-save triggered manually via Save icon in TranscriptionScreen TopBar
- Navigation route: `session_detail/{sessionId}` with typed NavArgument
- Session word count and duration displayed in history list

### Technical decisions
| Decision | Rationale |
|---|---|
| Room for sessions | Content grows unbounded; DataStore is for small key-value config, not variable-length blobs |
| `Intent.ACTION_SEND` for export | Zero implementation cost; shares as plain text to any app (WhatsApp, email, notes, Drive) |
| Manual save (not auto-save on stop) | Prevents polluting history with test/accidental sessions |

---

## Phase 6 — DOC 06: Polish, Tests & Release ✅
**Status:** Complete

### What was implemented
- `ReadingEngineTest`: 10 unit tests covering debounce, append, auto-follow, highlight, clear
- `TranscriptionViewModelTest`: 6 unit tests covering state machine, error handling, save behavior
- Test infrastructure: MockK + Turbine + `StandardTestDispatcher` + `advanceTimeBy`
- Recomposition optimization: AnnotatedString built only on state changes (not per scroll frame)
- `LaunchedEffect(scrollVersion)` prevents re-triggering scroll for already-handled versions

---

## Full Feature Matrix

| Feature | Status | Notes |
|---|---|---|
| Live speech transcription | ✅ | Android SpeechRecognizer, continuous mode |
| Smooth reading (no jumps) | ✅ | ReadingEngine + spring animation |
| Partial text display | ✅ | Italic, lighter color, configurable |
| Auto-scroll with pause | ✅ | Manual scroll → pause; FAB → resume |
| Font size settings | ✅ | 14–36sp, real-time preview |
| Line height settings | ✅ | ×1.2–×2.0 multiplier |
| Dark/light theme | ✅ | DataStore-persisted |
| High contrast mode | ✅ | Separate color scheme |
| Keep screen on | ✅ | Active during listening only |
| Session saving | ✅ | Manual, Room persistence |
| Session history | ✅ | LazyColumn, swipe-delete |
| Session detail / review | ✅ | Editable title |
| Export as text | ✅ | Android share sheet |
| Unit tests | ✅ | ReadingEngine + ViewModel |

## Known Bugs
- None confirmed — requires device testing

## Suggested Improvements (not implemented)
- On-device STT with ML Kit (no network dependency)
- Auto-save with configurable interval
- Word-level confidence visualization
- Export to PDF or Google Docs
- Search within session history
- Font family selection (Google Fonts)
- Landscape mode optimized layout (two-column: status + reading)
- App widget for quick-start

## APK Build Instructions

```bash
# Debug APK (development/sideload)
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew test

# Generate test report
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Next Steps for Production
1. Test on physical Android device (API 26+ with Google app installed)
2. Configure release signing keystore
3. Enable ProGuard and test for crashes
4. Upload to Google Play internal testing track
