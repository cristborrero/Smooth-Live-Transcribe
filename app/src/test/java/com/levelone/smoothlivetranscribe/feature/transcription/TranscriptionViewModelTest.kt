package com.levelone.smoothlivetranscribe.feature.transcription

import app.cash.turbine.test
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferences
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferencesRepository
import com.levelone.smoothlivetranscribe.domain.reading.ReadingEngine
import com.levelone.smoothlivetranscribe.domain.session.Session
import com.levelone.smoothlivetranscribe.domain.session.SessionRepository
import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionRepository
import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TranscriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val stateFlow = MutableSharedFlow<TranscriptionState>(replay = 0)

    private val transcriptionRepository = mockk<TranscriptionRepository>(relaxed = true) {
        every { transcriptionState } returns stateFlow
    }
    private val sessionRepository = mockk<SessionRepository>(relaxed = true)
    private val preferencesRepository = mockk<UserPreferencesRepository>(relaxed = true) {
        every { userPreferences } returns flowOf(UserPreferences())
    }
    private val readingEngine = ReadingEngine()

    private lateinit var viewModel: TranscriptionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TranscriptionViewModel(
            transcriptionRepository = transcriptionRepository,
            readingEngine = readingEngine,
            sessionRepository = sessionRepository,
            preferencesRepository = preferencesRepository
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle and not listening`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(TranscriptionState.Idle, state.recognitionStatus)
        assertFalse(state.isListening)
    }

    @Test
    fun `startListening sets isListening to true`() = runTest {
        viewModel.startListening()
        advanceTimeBy(100)
        assertTrue(viewModel.uiState.value.isListening)
    }

    @Test
    fun `stopListening sets isListening to false`() = runTest {
        viewModel.startListening()
        advanceTimeBy(100)
        viewModel.stopListening()
        advanceTimeBy(100)
        assertFalse(viewModel.uiState.value.isListening)
    }

    @Test
    fun `fatal error disables listening and sets error message`() = runTest {
        viewModel.startListening()
        advanceTimeBy(100)

        stateFlow.emit(TranscriptionState.Error("Mic permission denied", recoverable = false))
        advanceTimeBy(100)

        val state = viewModel.uiState.value
        assertFalse(state.isListening)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `clearError removes error message`() = runTest {
        viewModel.startListening()
        advanceTimeBy(100)
        stateFlow.emit(TranscriptionState.Error("Some error", recoverable = false))
        advanceTimeBy(100)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `saveCurrentSession saves session when confirmed text exists`() = runTest {
        viewModel.readingState.test {
            awaitItem() // Skip initial empty state

            viewModel.startListening()
            advanceTimeBy(100)

            // Inject some confirmed text via ReadingEngine
            readingEngine.onFinalResult("This is a test transcription.", this@runTest)
            
            // Wait for readingState flow to collect the new confirmed text
            val updatedState = awaitItem()
            assertEquals("This is a test transcription.", updatedState.confirmedText)

            viewModel.saveCurrentSession()
            advanceTimeBy(500)

            val sessionSlot = slot<Session>()
            coVerify { sessionRepository.saveSession(capture(sessionSlot)) }
            assertTrue(sessionSlot.captured.content.contains("test transcription"))
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveCurrentSession does nothing when content is blank`() = runTest {
        readingEngine.clear()
        viewModel.saveCurrentSession()
        advanceTimeBy(500)

        coVerify(exactly = 0) { sessionRepository.saveSession(any()) }
    }
}
