package com.levelone.smoothlivetranscribe.feature.transcription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferences
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferencesRepository
import com.levelone.smoothlivetranscribe.domain.reading.ReadingEngine
import com.levelone.smoothlivetranscribe.domain.reading.ReadingState
import com.levelone.smoothlivetranscribe.domain.session.Session
import com.levelone.smoothlivetranscribe.domain.session.SessionRepository
import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionRepository
import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * UI state for the TranscriptionScreen.
 *
 * Separating UI state from domain state (ReadingState) keeps the ViewModel
 * responsible for orchestration, not display details.
 */
data class TranscriptionUiState(
    val recognitionStatus: TranscriptionState = TranscriptionState.Idle,
    val isListening: Boolean = false,
    val errorMessage: String? = null,
    val sessionStartTimeMs: Long = 0L,
    val isSavingSession: Boolean = false,
    val lastSavedSessionId: String? = null
)

@HiltViewModel
class TranscriptionViewModel @Inject constructor(
    private val transcriptionRepository: TranscriptionRepository,
    private val readingEngine: ReadingEngine,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranscriptionUiState())
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    val readingState: StateFlow<ReadingState> = readingEngine.readingState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReadingState())

    val scrollVersion: StateFlow<Int> = readingEngine.scrollVersion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private var transcriptionJob: Job? = null

    fun startListening() {
        if (_uiState.value.isListening) return

        _uiState.update {
            it.copy(
                isListening = true,
                errorMessage = null,
                sessionStartTimeMs = System.currentTimeMillis()
            )
        }
        readingEngine.clear()

        transcriptionJob = viewModelScope.launch {
            transcriptionRepository.transcriptionState.collect { state ->
                _uiState.update { it.copy(recognitionStatus = state) }

                when (state) {
                    is TranscriptionState.PartialResult -> {
                        if (userPreferences.value.showPartialText) {
                            readingEngine.onPartialResult(state.text, viewModelScope)
                        }
                    }
                    is TranscriptionState.FinalResult -> {
                        readingEngine.onFinalResult(state.text, viewModelScope)
                    }
                    is TranscriptionState.Error -> {
                        if (!state.recoverable) {
                            _uiState.update {
                                it.copy(isListening = false, errorMessage = state.message)
                            }
                        }
                    }
                    else -> { /* Other states handled by status display */ }
                }
            }
        }

        viewModelScope.launch {
            transcriptionRepository.startListening()
        }
    }

    fun stopListening() {
        transcriptionJob?.cancel()
        transcriptionJob = null
        viewModelScope.launch {
            transcriptionRepository.stopListening()
        }
        _uiState.update {
            it.copy(
                isListening = false,
                recognitionStatus = TranscriptionState.Idle
            )
        }
    }

    fun pauseAutoFollow() = readingEngine.pauseAutoFollow()
    fun resumeAutoFollow() = readingEngine.resumeAutoFollow()

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun saveCurrentSession() {
        val content = readingState.value.confirmedText
        if (content.isBlank()) return

        val startTime = _uiState.value.sessionStartTimeMs
        val duration = System.currentTimeMillis() - startTime
        val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            .format(Date(startTime))

        _uiState.update { it.copy(isSavingSession = true) }

        viewModelScope.launch {
            val session = Session(
                id = UUID.randomUUID().toString(),
                title = "Session — $dateStr",
                content = content,
                createdAt = startTime,
                durationMs = duration
            )
            sessionRepository.saveSession(session)
            _uiState.update {
                it.copy(isSavingSession = false, lastSavedSessionId = session.id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        transcriptionRepository.release()
    }
}
