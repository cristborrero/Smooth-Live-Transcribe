package com.levelone.smoothlivetranscribe.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferences
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setFontSize(size: Float) = viewModelScope.launch { repository.setFontSize(size) }
    fun setLineHeight(h: Float) = viewModelScope.launch { repository.setLineHeight(h) }
    fun setHighContrast(v: Boolean) = viewModelScope.launch { repository.setHighContrast(v) }
    fun setDarkTheme(v: Boolean) = viewModelScope.launch { repository.setDarkTheme(v) }
    fun setScrollSpeed(v: Float) = viewModelScope.launch { repository.setScrollSpeed(v) }
    fun setShowPartialText(v: Boolean) = viewModelScope.launch { repository.setShowPartialText(v) }
    fun setHighlightLastChunk(v: Boolean) = viewModelScope.launch { repository.setHighlightLastChunk(v) }
    fun setKeepScreenOn(v: Boolean) = viewModelScope.launch { repository.setKeepScreenOn(v) }
}
