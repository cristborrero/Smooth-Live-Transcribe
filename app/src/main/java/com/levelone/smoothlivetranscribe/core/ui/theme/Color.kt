package com.levelone.smoothlivetranscribe.core.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette — deep teal/cyan for a calm, focused reading experience
val Primary = Color(0xFF00897B)        // Teal 600
val PrimaryDark = Color(0xFF00695C)    // Teal 800
val PrimaryLight = Color(0xFF4DB6AC)   // Teal 300
val OnPrimary = Color(0xFFFFFFFF)

// Secondary — warm amber accent for interactive elements
val Secondary = Color(0xFFFFB300)      // Amber 600
val OnSecondary = Color(0xFF1A1A1A)

// Surface colors — dark mode optimized for long reading sessions
val SurfaceDark = Color(0xFF121212)    // Near-black for dark mode
val SurfaceVariantDark = Color(0xFF1E1E1E)
val OnSurfaceDark = Color(0xFFE8F5E9) // Slightly warm white — reduces eye strain

// Surface colors — light mode
val SurfaceLight = Color(0xFFF5F5F5)
val SurfaceVariantLight = Color(0xFFE0F2F1) // Teal-tinted surface
val OnSurfaceLight = Color(0xFF1A2027)       // Dark blue-gray

// Partial text (interim transcription) colors
// These must be visually distinct but not jarring — muted version of primary
val PartialTextDark = Color(0xFF80CBC4)   // Teal 200 — soft, ephemeral feel
val PartialTextLight = Color(0xFF00796B)  // Teal 700 — readable on light bg

// Error colors
val Error = Color(0xFFCF6679)
val OnError = Color(0xFF1A1A1A)

// High contrast overrides
val HighContrastBackground = Color(0xFF000000)
val HighContrastOnBackground = Color(0xFFFFFFFF)
val HighContrastPrimary = Color(0xFF64FFDA)  // Teal A200
