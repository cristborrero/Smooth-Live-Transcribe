package com.levelone.smoothlivetranscribe.data.speech

import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionRepository
import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements [TranscriptionRepository] using [SpeechRecognizerWrapper].
 *
 * Maps low-level [SpeechEvent]s to domain-level [TranscriptionState]s.
 * This mapping layer ensures the domain and UI are completely decoupled
 * from the Android speech API.
 *
 * The _isListening flag tracks whether the user explicitly requested transcription.
 * This is separate from the recognizer's own state — e.g., the recognizer can
 * be in "restarting" state while the user still expects transcription to continue.
 */
@Singleton
class SpeechTranscriptionRepository @Inject constructor(
    private val recognizerWrapper: SpeechRecognizerWrapper
) : TranscriptionRepository {

    private val _isListening = MutableStateFlow(false)

    override val transcriptionState: Flow<TranscriptionState> =
        recognizerWrapper.speechEvents().map { event ->
            mapEventToState(event)
        }

    override suspend fun startListening() {
        _isListening.value = true
        // The Flow starts the recognizer when collected — nothing to do here
        // startListening is called by the ViewModel collecting the Flow
    }

    override suspend fun stopListening() {
        _isListening.value = false
        recognizerWrapper.stopListening()
    }

    override fun release() {
        recognizerWrapper.release()
    }

    private fun mapEventToState(event: SpeechEvent): TranscriptionState = when (event) {
        is SpeechEvent.ReadyForSpeech -> TranscriptionState.Listening
        is SpeechEvent.BeginningOfSpeech -> TranscriptionState.Listening
        is SpeechEvent.EndOfSpeech -> TranscriptionState.Listening  // Session continues
        is SpeechEvent.PartialResult -> TranscriptionState.PartialResult(event.text)
        is SpeechEvent.FinalResult -> TranscriptionState.FinalResult(event.text)
        is SpeechEvent.Error -> when {
            event.message.contains("No speech") -> TranscriptionState.NoSpeechDetected
            event.recoverable -> TranscriptionState.Error(event.message, recoverable = true)
            else -> TranscriptionState.Error(event.message, recoverable = false)
        }
    }
}
