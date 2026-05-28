package com.levelone.smoothlivetranscribe.domain.transcription

/**
 * Represents all possible states of the speech recognition lifecycle.
 *
 * Design decision: sealed class (not enum) because some states carry data
 * (e.g., PartialResult, FinalResult, Error) and using an enum would force us
 * to store that data externally — breaking cohesion.
 *
 * State machine:
 *   Idle → RequestingPermission → Ready → Listening
 *   Listening → PartialResult → Listening (loop while speaking)
 *   Listening → FinalResult → Ready (auto-restart)
 *   Listening → Error → Ready (recoverable) or Idle (fatal)
 *   Listening → NoSpeechDetected → Ready (auto-restart)
 */
sealed class TranscriptionState {

    /** App has just started or recognizer was stopped intentionally. */
    object Idle : TranscriptionState()

    /** We are asking the user for RECORD_AUDIO permission. */
    object RequestingPermission : TranscriptionState()

    /** Permission granted, recognizer initialized, ready to start. */
    object Ready : TranscriptionState()

    /** Actively listening and processing audio. */
    object Listening : TranscriptionState()

    /**
     * Interim transcription result received.
     * This text IS NOT FINAL — the recognizer may still revise it.
     * Display with secondary visual treatment (lighter color, italic).
     *
     * @param text The current partial transcription.
     */
    data class PartialResult(val text: String) : TranscriptionState()

    /**
     * A stable, confirmed transcription segment.
     * This text will NOT change — safe to append to the confirmed buffer.
     *
     * @param text The confirmed transcription segment.
     */
    data class FinalResult(val text: String) : TranscriptionState()

    /**
     * A recoverable or fatal error occurred.
     *
     * @param message Human-readable error description.
     * @param recoverable If true, the recognizer will attempt to restart automatically.
     */
    data class Error(val message: String, val recoverable: Boolean = true) : TranscriptionState()

    /** SpeechRecognizer timed out without detecting speech. Will auto-restart. */
    object NoSpeechDetected : TranscriptionState()
}
