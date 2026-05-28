package com.levelone.smoothlivetranscribe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levelone.smoothlivetranscribe.feature.history.SessionDetailScreen
import com.levelone.smoothlivetranscribe.feature.history.SessionHistoryScreen
import com.levelone.smoothlivetranscribe.feature.settings.SettingsScreen
import com.levelone.smoothlivetranscribe.feature.transcription.TranscriptionScreen

/** Navigation route constants. Single source of truth for all destinations. */
object Routes {
    const val TRANSCRIPTION = "transcription"
    const val SETTINGS = "settings"
    const val SESSION_HISTORY = "session_history"
    const val SESSION_DETAIL = "session_detail/{sessionId}"

    fun sessionDetail(sessionId: String) = "session_detail/$sessionId"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.TRANSCRIPTION
    ) {
        composable(Routes.TRANSCRIPTION) {
            TranscriptionScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToHistory = { navController.navigate(Routes.SESSION_HISTORY) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SESSION_HISTORY) {
            SessionHistoryScreen(
                onBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(Routes.sessionDetail(sessionId))
                }
            )
        }

        composable(
            route = Routes.SESSION_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            SessionDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
