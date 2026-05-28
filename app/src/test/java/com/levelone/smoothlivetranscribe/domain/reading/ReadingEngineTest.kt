package com.levelone.smoothlivetranscribe.domain.reading

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ReadingEngine — the core anti-jump logic.
 *
 * We test the engine in isolation (no Android deps) because the domain layer
 * is pure Kotlin. This is by design — the domain MUST be testable without a device.
 *
 * Key behaviors under test:
 * - Final results append correctly and clear partial text
 * - Partial results are debounced (multiple rapid calls → single update)
 * - Auto-follow pause/resume behavior
 * - Clear resets all state
 * - Scroll version increments on the right events
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReadingEngineTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var engine: ReadingEngine

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        engine = ReadingEngine()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onFinalResult appends to confirmedText`() = runTest {
        engine.onFinalResult("Hello world", this)
        advanceTimeBy(1000)

        val state = engine.readingState.value
        assertEquals("Hello world", state.confirmedText)
        assertEquals("", state.partialText)
    }

    @Test
    fun `multiple final results concatenate with spaces`() = runTest {
        engine.onFinalResult("First sentence.", this)
        engine.onFinalResult("Second sentence.", this)
        advanceTimeBy(1000)

        val state = engine.readingState.value
        assertEquals("First sentence. Second sentence.", state.confirmedText)
    }

    @Test
    fun `final result clears partial text`() = runTest {
        // Set up a partial first
        engine.onPartialResult("partial text", this)
        advanceTimeBy(200) // let debounce fire

        // Then a final arrives
        engine.onFinalResult("confirmed text", this)
        advanceTimeBy(1000)

        val state = engine.readingState.value
        assertEquals("confirmed text", state.confirmedText)
        assertEquals("", state.partialText) // partial must be cleared
    }

    @Test
    fun `partial results are debounced — only last update within window applies`() = runTest {
        // Fire 3 partial results in rapid succession within debounce window
        engine.onPartialResult("partial 1", this)
        engine.onPartialResult("partial 2", this)
        engine.onPartialResult("partial 3", this)

        // Before debounce fires — state should still be empty
        assertEquals("", engine.readingState.value.partialText)

        // After debounce window (150ms) — only last partial applies
        advanceTimeBy(200)
        assertEquals("partial 3", engine.readingState.value.partialText)
    }

    @Test
    fun `blank final result is ignored`() = runTest {
        engine.onFinalResult("  ", this) // blank/whitespace
        advanceTimeBy(1000)
        assertEquals("", engine.readingState.value.confirmedText)
    }

    @Test
    fun `pauseAutoFollow disables auto follow`() = runTest {
        assertTrue(engine.readingState.value.autoFollowEnabled)
        engine.pauseAutoFollow()
        assertFalse(engine.readingState.value.autoFollowEnabled)
    }

    @Test
    fun `resumeAutoFollow re-enables and increments scroll version`() = runTest {
        engine.pauseAutoFollow()
        val versionBefore = engine.scrollVersion.value

        engine.resumeAutoFollow()

        assertTrue(engine.readingState.value.autoFollowEnabled)
        assertTrue(engine.scrollVersion.value > versionBefore)
    }

    @Test
    fun `clear resets all state`() = runTest {
        engine.onFinalResult("Some text", this)
        engine.onPartialResult("partial", this)
        advanceTimeBy(300)

        engine.clear()

        val state = engine.readingState.value
        assertEquals("", state.confirmedText)
        assertEquals("", state.partialText)
        assertEquals(0, engine.scrollVersion.value)
    }

    @Test
    fun `scroll version increments on final result`() = runTest {
        val before = engine.scrollVersion.value
        engine.onFinalResult("New text", this)
        advanceTimeBy(100)

        assertTrue(engine.scrollVersion.value > before)
    }

    @Test
    fun `mergedVisibleText combines confirmed and partial`() = runTest {
        engine.onFinalResult("Confirmed.", this)
        advanceTimeBy(200)
        engine.onPartialResult("partial...", this)
        advanceTimeBy(200)

        val state = engine.readingState.value
        assertTrue(state.mergedVisibleText.contains("Confirmed."))
        assertTrue(state.mergedVisibleText.contains("partial..."))
    }

    @Test
    fun `highlight is set on final result and clears after duration`() = runTest {
        engine.onFinalResult("New chunk", this)

        // Immediately after — highlight should be on
        assertTrue(engine.readingState.value.isHighlightingLastChunk)
        assertEquals("New chunk", engine.readingState.value.lastConfirmedChunk)

        // After 900ms — highlight should be cleared (duration is 800ms)
        advanceTimeBy(900)
        assertFalse(engine.readingState.value.isHighlightingLastChunk)
    }
}
