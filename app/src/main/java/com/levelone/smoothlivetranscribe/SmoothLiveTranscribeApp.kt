package com.levelone.smoothlivetranscribe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class annotated with @HiltAndroidApp.
 * This triggers Hilt's code generation and creates the application-level component.
 * Without this, Hilt injection will not work anywhere in the app.
 */
@HiltAndroidApp
class SmoothLiveTranscribeApp : Application()
