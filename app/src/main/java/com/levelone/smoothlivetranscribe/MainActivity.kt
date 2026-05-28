package com.levelone.smoothlivetranscribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.levelone.smoothlivetranscribe.core.ui.theme.SmoothLiveTranscribeTheme
import com.levelone.smoothlivetranscribe.data.preferences.UserPreferencesRepository
import com.levelone.smoothlivetranscribe.ui.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single Activity. All navigation is handled by Navigation Compose.
 *
 * Theme is driven by UserPreferences (dark/highContrast) from DataStore,
 * so changes in Settings are reflected immediately without restart.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by preferencesRepository.userPreferences
                .collectAsStateWithLifecycle(initialValue = com.levelone.smoothlivetranscribe.data.preferences.UserPreferences())

            SmoothLiveTranscribeTheme(
                darkTheme = preferences.darkTheme,
                highContrast = preferences.highContrast
            ) {
                AppNavGraph()
            }
        }
    }
}
