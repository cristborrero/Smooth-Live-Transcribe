package com.levelone.smoothlivetranscribe.data.preferences

/**
 * All user-configurable reading preferences.
 *
 * Defaults are calibrated for a church/conference setting:
 * - Large font (22sp) for reading at arm's length
 * - Generous line height (1.6) for sustained reading without eye fatigue
 * - Dark theme by default — reduces screen glare in dimly lit venues
 * - Keep screen on — sessions can last 1-2 hours
 * - Show partial text — gives the user a sense of forward momentum
 */
data class UserPreferences(
    /** Base font size in SP. Range: 14..36 */
    val fontSizeSp: Float = 22f,

    /** Line height multiplier. Range: 1.2..2.0 */
    val lineHeightMultiplier: Float = 1.6f,

    /** High contrast mode for maximum readability */
    val highContrast: Boolean = false,

    /** Force dark theme regardless of system setting */
    val darkTheme: Boolean = true,

    /** Auto-scroll speed multiplier. Range: 0.5..2.0 (1.0 = default spring behavior) */
    val scrollSpeedMultiplier: Float = 1.0f,

    /** Whether to show partial/interim text */
    val showPartialText: Boolean = true,

    /** Highlight the last confirmed chunk briefly when finalized */
    val highlightLastChunk: Boolean = true,

    /** Keep screen on while transcription is active */
    val keepScreenOn: Boolean = true
)
