package com.levelone.smoothlivetranscribe.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelone.smoothlivetranscribe.domain.session.Session
import com.levelone.smoothlivetranscribe.domain.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    val sessions: StateFlow<List<Session>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(id: String) = viewModelScope.launch {
        repository.deleteSession(id)
    }
}
