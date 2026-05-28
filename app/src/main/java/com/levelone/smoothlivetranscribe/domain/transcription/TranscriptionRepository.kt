package com.levelone.smoothlivetranscribe.domain.transcription

import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for speech recognition.
 *
 * This interface deliberately hides all Android-specific details
 * (SpeechRecognizer, RecognitionListener, etc.) from the UI and domain layers.
 *
 * The implementation lives in the data layer and is injected via Hilt.
 * This keeps the domain pure and testable without Android instrumentation.
 */
interface TranscriptionRepository {

    /**
     * A cold Flow of transcription states.
     * Collecting this Flow does NOT start the recognizer.
     * Call [startListening] to begin.
     */
    val transcriptionState: Flow<TranscriptionState>

    /**
     * Starts the speech recognizer.
     * Emits [TranscriptionState.Listening] when active.
     * Safe to call repeatedly — will stop current session if already running.
     */
    suspend fun startListening()

    /**
     * Stops the speech recognizer.
     * Emits [TranscriptionState.Idle] after cleanup.
     */
    suspend fun stopListening()

    /**
     * Releases all resources held by the underlying recognizer.
     * MUST be called when the owning ViewModel is cleared.
     */
    fun release()
}
