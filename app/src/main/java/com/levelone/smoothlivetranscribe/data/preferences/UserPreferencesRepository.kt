package com.levelone.smoothlivetranscribe.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property creates a single DataStore instance per process
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

/**
 * Repository for reading and writing [UserPreferences] via DataStore.
 *
 * Why DataStore over SharedPreferences?
 * - Type-safe keys via preferencesKey<T>()
 * - Coroutine + Flow based — no blocking I/O on main thread
 * - Atomic writes — no partial-write corruption on process kill
 * - Suspending edit() function — fits naturally into ViewModel coroutines
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_HEIGHT = floatPreferencesKey("line_height")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val SHOW_PARTIAL = booleanPreferencesKey("show_partial")
        val HIGHLIGHT_LAST = booleanPreferencesKey("highlight_last")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            fontSizeSp = prefs[Keys.FONT_SIZE] ?: 22f,
            lineHeightMultiplier = prefs[Keys.LINE_HEIGHT] ?: 1.6f,
            highContrast = prefs[Keys.HIGH_CONTRAST] ?: false,
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            scrollSpeedMultiplier = prefs[Keys.SCROLL_SPEED] ?: 1.0f,
            showPartialText = prefs[Keys.SHOW_PARTIAL] ?: true,
            highlightLastChunk = prefs[Keys.HIGHLIGHT_LAST] ?: true,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true
        )
    }

    suspend fun setFontSize(size: Float) = context.dataStore.edit {
        it[Keys.FONT_SIZE] = size.coerceIn(14f, 36f)
    }

    suspend fun setLineHeight(multiplier: Float) = context.dataStore.edit {
        it[Keys.LINE_HEIGHT] = multiplier.coerceIn(1.2f, 2.0f)
    }

    suspend fun setHighContrast(enabled: Boolean) = context.dataStore.edit {
        it[Keys.HIGH_CONTRAST] = enabled
    }

    suspend fun setDarkTheme(enabled: Boolean) = context.dataStore.edit {
        it[Keys.DARK_THEME] = enabled
    }

    suspend fun setScrollSpeed(multiplier: Float) = context.dataStore.edit {
        it[Keys.SCROLL_SPEED] = multiplier.coerceIn(0.5f, 2.0f)
    }

    suspend fun setShowPartialText(show: Boolean) = context.dataStore.edit {
        it[Keys.SHOW_PARTIAL] = show
    }

    suspend fun setHighlightLastChunk(highlight: Boolean) = context.dataStore.edit {
        it[Keys.HIGHLIGHT_LAST] = highlight
    }

    suspend fun setKeepScreenOn(keep: Boolean) = context.dataStore.edit {
        it[Keys.KEEP_SCREEN_ON] = keep
    }
}
