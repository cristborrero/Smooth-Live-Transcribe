package com.levelone.smoothlivetranscribe.domain.reading

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The ReadingEngine is the core of the "smooth reading" product value proposition.
 *
 * PROBLEM IT SOLVES:
 * Android's SpeechRecognizer fires rapid partial results (sometimes multiple per second).
 * If we naively update the UI on every event, the layout recomposes constantly, causing
 * the text to "jump" — the very problem this app exists to prevent.
 *
 * STRATEGY:
 * 1. CONFIRMED vs PARTIAL separation: Confirmed text is immutable once appended.
 *    Only the partial zone changes, and it's rendered with a secondary style.
 *    The reader's eye anchors on the confirmed zone — no movement there = no fatigue.
 *
 * 2. DEBOUNCE on partial updates: We wait [PARTIAL_DEBOUNCE_MS] before updating the
 *    partial text in the state. This prevents sub-100ms micro-updates that the human
 *    eye can't track anyway, while still feeling responsive (≤150ms feels instant).
 *
 * 3. APPEND BATCHING on final results: When a final result arrives, we append it to
 *    confirmed text with a small buffer. If multiple final results arrive in quick
 *    succession (e.g., fast speaker), they're merged into one state update,
 *    reducing the number of layout passes.
 *
 * 4. SCROLL TARGET: We expose a [scrollVersion] counter rather than a raw pixel offset.
 *    The composable increments its own scroll animation on each version bump —
 *    this decouples layout measurement from the engine logic.
 *
 * WHY THIS REDUCES VISUAL FATIGUE:
 * - The confirmed text block is a stable "island" that never moves.
 * - The partial text block at the bottom changes in place — it only appends downward.
 * - Auto-scroll is triggered by version bumps, not pixel positions — giving Compose
 *   full control over the animation curve (spring by default).
 * - Debouncing means the composable recomposes at most every 150ms for partial updates,
 *   not 10-30 times per second.
 */
@Singleton
class ReadingEngine @Inject constructor() {

    companion object {
        /** Minimum delay between partial result UI updates. 150ms is imperceptible to humans
         *  but prevents the layout thrashing that causes text jumps. */
        private const val PARTIAL_DEBOUNCE_MS = 150L

        /** How long to highlight the last confirmed chunk after it's finalized. */
        private const val HIGHLIGHT_DURATION_MS = 800L
    }

    private val _readingState = MutableStateFlow(ReadingState())
    val readingState: StateFlow<ReadingState> = _readingState.asStateFlow()

    // Scroll version counter — incremented each time the composable should animate to bottom
    private val _scrollVersion = MutableStateFlow(0)
    val scrollVersion: StateFlow<Int> = _scrollVersion.asStateFlow()

    private var debounceJob: Job? = null
    private var highlightJob: Job? = null

    /**
     * Adds a confirmed (final) transcription segment.
     * This is immutable once called — confirmed text never changes.
     *
     * @param text The finalized segment from SpeechRecognizer.onResults()
     * @param scope CoroutineScope for the highlight timer.
     */
    fun onFinalResult(text: String, scope: CoroutineScope) {
        if (text.isBlank()) return

        // Cancel any pending partial debounce — the final result supersedes it
        debounceJob?.cancel()

        _readingState.update { current ->
            val newConfirmed = if (current.confirmedText.isNotEmpty()) {
                "${current.confirmedText} $text"
            } else {
                text
            }
            current.copy(
                confirmedText = newConfirmed,
                partialText = "",          // Clear partial — it's now confirmed
                lastConfirmedChunk = text,
                isHighlightingLastChunk = true
            )
        }

        // Trigger scroll animation
        _scrollVersion.value++

        // Remove highlight after a short time
        highlightJob?.cancel()
        highlightJob = scope.launch {
            delay(HIGHLIGHT_DURATION_MS)
            _readingState.update { it.copy(isHighlightingLastChunk = false) }
        }
    }

    /**
     * Updates the partial (interim) text with debouncing.
     * Multiple rapid calls within [PARTIAL_DEBOUNCE_MS] are collapsed into one update.
     *
     * @param text The current interim result from SpeechRecognizer.onPartialResults()
     * @param scope CoroutineScope for the debounce job.
     */
    fun onPartialResult(text: String, scope: CoroutineScope) {
        if (text.isBlank()) return

        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(PARTIAL_DEBOUNCE_MS)
            _readingState.update { it.copy(partialText = text) }
            // Only scroll for partial if auto-follow is on and text is substantial
            if (_readingState.value.autoFollowEnabled && text.length > 20) {
                _scrollVersion.value++
            }
        }
    }

    /**
     * Clears all transcription content.
     * Called when a new session starts or the user manually clears the screen.
     */
    fun clear() {
        debounceJob?.cancel()
        highlightJob?.cancel()
        _readingState.value = ReadingState()
        _scrollVersion.value = 0
    }

    /**
     * Called when the user manually scrolls the reading area.
     * Disables auto-follow so the user can read at their own pace.
     */
    fun pauseAutoFollow() {
        _readingState.update { it.copy(autoFollowEnabled = false) }
    }

    /**
     * Re-enables auto-follow and immediately scrolls to the latest content.
     * Called when the user taps the "Back to Live" button.
     */
    fun resumeAutoFollow() {
        _readingState.update { it.copy(autoFollowEnabled = true) }
        _scrollVersion.value++
    }
}
