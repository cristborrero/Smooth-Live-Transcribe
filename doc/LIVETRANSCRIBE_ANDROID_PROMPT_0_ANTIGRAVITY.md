# LIVETRANSCRIBE ANDROID — PROMPT 0 — Antigravity Kickoff

> Paste this prompt directly into Antigravity's Agent Manager (Planning Mode recommended).
> Language: English — models follow technical instructions more precisely in English.

---

## Mission

You are a senior Android engineer working inside the Antigravity IDE.
Your job is to build a production-quality Android app called **Smooth Live Transcribe** from scratch, following a series of documentation files stored in `/doc/`.

This app solves a real problem: existing transcription apps like Google Live Transcribe cause text to jump abruptly when new words are added, making real-time reading painful. This app must feel like a smooth teleprompter — text flows forward naturally, never jumps, never causes eye strain.

The primary user is a Spanish-speaking person learning English who reads live transcriptions during church services, conferences, and lectures.

---

## Repository setup

Before reading any doc files, do the following:

1. Create a new Android project with these exact settings:
   - **App name:** Smooth Live Transcribe
   - **Package name:** com.levelone.smoothlivetranscribe
   - **Language:** Kotlin
   - **Min SDK:** 26
   - **Target SDK:** 35
   - **Build system:** Gradle with Kotlin DSL
   - **UI framework:** Jetpack Compose + Material 3
2. Create the folder `/doc/` in the root of the project if it does not exist.
3. Confirm the project compiles before proceeding.

---

## Documentation files

The following files exist or will be placed inside `/doc/`. Read each one in strict order before implementing:

| Order | File |
|---|---|
| 1 | LIVETRANSCRIBE_ANDROID_DOC_01_VISION_ARCHITECTURE.md |
| 2 | LIVETRANSCRIBE_ANDROID_DOC_02_AUDIO_PERMISSIONS_TRANSCRIPTION_BASE.md |
| 3 | LIVETRANSCRIBE_ANDROID_DOC_03_SMOOTH_READING_ENGINE.md |
| 4 | LIVETRANSCRIBE_ANDROID_DOC_04_READING_MODE_SETTINGS.md |
| 5 | LIVETRANSCRIBE_ANDROID_DOC_05_HISTORY_EXPORT_SESSION.md |
| 6 | LIVETRANSCRIBE_ANDROID_DOC_06_POLISH_TESTING_RELEASE.md |

---

## Execution rules

1. **Read the full doc before writing a single line of code.** Summarize what you understand before starting each phase.
2. **Execute all docs in sequence without stopping**, unless a doc explicitly requires an API key, external service credential, or hardware decision that cannot be inferred. In that case, pause only for that blocker and document it clearly, then continue with everything else.
3. After each doc is complete, **update or create `status_project.md`** at the project root with:
   - Phase completed
   - What was implemented
   - Technical decisions made and why
   - Known issues or tech debt
   - Next phase preview
4. **Never break existing functionality** when moving to the next doc.
5. **Commit-like checkpoints:** After each doc, leave a clearly marked comment block in the main entry file with the phase number and summary.
6. Do not invent features not described in the docs. If you spot an improvement, document it in `status_project.md` under "Suggested improvements" but do not implement it unless the doc explicitly allows it.

---

## Architecture rules

- Clean layered architecture: `ui` → `domain` → `data`
- Compose for all UI — no XML layouts
- ViewModel per feature screen
- Kotlin Coroutines + StateFlow for state management
- Hilt for dependency injection
- DataStore for user preferences
- SpeechRecognizer (Android native) as transcription engine — no third-party STT unless a doc specifically instructs otherwise
- No over-engineering: clean modules within a single `app` module is fine for this scale

---

## UX contract (non-negotiable)

These rules define the product. Never violate them:

- Text in the reading area must **never jump abruptly** when new content arrives.
- Partial (interim) results must be visually distinct from confirmed text.
- Auto-scroll must use smooth spring or tween animation — never `scrollTo()` with no animation.
- If the user manually scrolls, auto-follow must pause and a "back to live" button must appear.
- Reading area must support large font sizes for comfortable long sessions.
- Keep-screen-awake must be active during transcription.

---

## Final deliverable

When all 6 docs are complete:

1. Generate a final `status_project.md` with:
   - Full feature list and completion status
   - Known bugs
   - Suggested next improvements
   - Exact steps to build and sideload the APK on an Android device
2. Provide build instructions for generating a debug APK.
3. Confirm the project compiles clean with no errors.

---

## Start

Begin now. Read DOC 01 first. Summarize your understanding of the architecture plan, then implement it.
