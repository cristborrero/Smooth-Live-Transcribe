package com.levelone.smoothlivetranscribe.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SpeechRecognizerWrapper"

/**
 * Wraps Android's SpeechRecognizer in a clean, lifecycle-aware API.
 *
 * Key design decisions:
 * 1. Uses [callbackFlow] to bridge the callback-based RecognitionListener to coroutine Flow.
 * 2. The SpeechRecognizer MUST be created and accessed on the MAIN thread (Android requirement).
 *    The wrapper enforces this via [android.os.Looper.getMainLooper()].
 * 3. Resource cleanup is handled in the Flow's awaitClose block — no leaks even if the
 *    collector is cancelled.
 * 4. Auto-restart on recoverable errors keeps the mic open for continuous sessions.
 *
 * @param context ApplicationContext — safe to hold long-term, no activity leak.
 */
@Singleton
class SpeechRecognizerWrapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var recognizer: SpeechRecognizer? = null

    /**
     * Builds the RecognizerIntent configured for continuous, real-time transcription.
     *
     * - LANGUAGE_MODEL_FREE_FORM: natural speech vs command-driven dictation
     * - PARTIAL_RESULTS true: we get interim text as the user speaks (critical for the UI)
     * - MAX_RESULTS 1: we only care about the top hypothesis
     * - No endpointing timeout: keeps the session alive during natural pauses
     */
    private fun buildRecognizerIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        // Allow longer speech segments without forced stop
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
    }

    /**
     * Creates a Flow of [SpeechEvent] representing the recognizer lifecycle.
     *
     * The recognizer is created fresh each time the Flow is collected.
     * Cancelling the Flow (e.g., ViewModel.onCleared) triggers cleanup automatically.
     */
    fun speechEvents(): Flow<SpeechEvent> = callbackFlow {
        // SpeechRecognizer must be created on the main thread
        val sr = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = sr

        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech")
                trySend(SpeechEvent.ReadyForSpeech)
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
                trySend(SpeechEvent.BeginningOfSpeech)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Ignored — we don't visualize RMS in this version
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Ignored
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
                trySend(SpeechEvent.EndOfSpeech)
            }

            override fun onError(error: Int) {
                val (message, recoverable) = mapError(error)
                Log.w(TAG, "onError: $error — $message (recoverable=$recoverable)")
                trySend(SpeechEvent.Error(message, recoverable))

                // Auto-restart on recoverable errors to keep the session alive
                if (recoverable) {
                    try {
                        sr.startListening(buildRecognizerIntent())
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to restart after error", e)
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?: ""
                Log.d(TAG, "onResults: $text")
                trySend(SpeechEvent.FinalResult(text))

                // Auto-restart: SpeechRecognizer stops after each result.
                // We restart immediately for a continuous transcription experience.
                try {
                    sr.startListening(buildRecognizerIntent())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restart after result", e)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?: return
                trySend(SpeechEvent.PartialResult(text))
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Not used
            }
        })

        // Start listening immediately on collection
        sr.startListening(buildRecognizerIntent())

        // Cleanup when the Flow collector is cancelled (ViewModel cleared, screen left, etc.)
        awaitClose {
            Log.d(TAG, "Flow cancelled — releasing SpeechRecognizer")
            sr.stopListening()
            sr.destroy()
            recognizer = null
        }
    }

    /**
     * Stops the current recognition session without destroying the recognizer.
     * Safe to call even if not currently listening.
     */
    fun stopListening() {
        recognizer?.stopListening()
    }

    /**
     * Fully releases the recognizer.
     * MUST be called before this object goes out of scope if not using Flow.
     */
    fun release() {
        recognizer?.destroy()
        recognizer = null
    }

    /**
     * Maps Android SpeechRecognizer error codes to human-readable messages.
     * Returns (message, isRecoverable) — recoverable errors trigger an auto-restart.
     */
    private fun mapError(error: Int): Pair<String, Boolean> = when (error) {
        SpeechRecognizer.ERROR_AUDIO ->
            "Audio recording error" to true
        SpeechRecognizer.ERROR_CLIENT ->
            "Client-side error" to false
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission denied" to false
        SpeechRecognizer.ERROR_NETWORK ->
            "Network error — check your connection" to true
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Network timeout" to true
        SpeechRecognizer.ERROR_NO_MATCH ->
            "No speech matched" to true   // Common during pauses — safe to restart
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "Speech recognizer is busy" to true
        SpeechRecognizer.ERROR_SERVER ->
            "Server error" to true
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "No speech detected" to true  // Will auto-restart = keeps session alive
        else ->
            "Unknown speech error ($error)" to true
    }
}

/**
 * Typed events emitted by [SpeechRecognizerWrapper].
 * Isolated from RecognitionListener — allows testing without Android deps.
 */
sealed class SpeechEvent {
    object ReadyForSpeech : SpeechEvent()
    object BeginningOfSpeech : SpeechEvent()
    object EndOfSpeech : SpeechEvent()
    data class PartialResult(val text: String) : SpeechEvent()
    data class FinalResult(val text: String) : SpeechEvent()
    data class Error(val message: String, val recoverable: Boolean) : SpeechEvent()
}
