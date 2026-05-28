package com.levelone.smoothlivetranscribe.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Live Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Preview",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "The quick brown fox jumps over the lazy dog",
                        fontSize = prefs.fontSizeSp.sp,
                        lineHeight = (prefs.fontSizeSp * prefs.lineHeightMultiplier).sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "and keeps on running…",
                        fontSize = (prefs.fontSizeSp * 0.95f).sp,
                        lineHeight = (prefs.fontSizeSp * prefs.lineHeightMultiplier).sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }

            // Text Size
            SettingsSection(title = "Text") {
                SettingsSlider(
                    label = "Font Size",
                    value = prefs.fontSizeSp,
                    valueRange = 14f..36f,
                    displayValue = "${prefs.fontSizeSp.toInt()} sp",
                    onValueChange = { viewModel.setFontSize(it) }
                )
                SettingsSlider(
                    label = "Line Height",
                    value = prefs.lineHeightMultiplier,
                    valueRange = 1.2f..2.0f,
                    displayValue = "×${"%.1f".format(prefs.lineHeightMultiplier)}",
                    onValueChange = { viewModel.setLineHeight(it) }
                )
            }

            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsToggle(
                    label = "Dark Theme",
                    checked = prefs.darkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
                HorizontalDivider()
                SettingsToggle(
                    label = "High Contrast",
                    description = "Maximum readability for bright environments",
                    checked = prefs.highContrast,
                    onCheckedChange = { viewModel.setHighContrast(it) }
                )
            }

            // Reading Behavior
            SettingsSection(title = "Reading Behavior") {
                SettingsSlider(
                    label = "Auto-scroll Speed",
                    value = prefs.scrollSpeedMultiplier,
                    valueRange = 0.5f..2.0f,
                    displayValue = "×${"%.1f".format(prefs.scrollSpeedMultiplier)}",
                    onValueChange = { viewModel.setScrollSpeed(it) }
                )
                HorizontalDivider()
                SettingsToggle(
                    label = "Show Partial Text",
                    description = "Preview words as they're being recognized",
                    checked = prefs.showPartialText,
                    onCheckedChange = { viewModel.setShowPartialText(it) }
                )
                HorizontalDivider()
                SettingsToggle(
                    label = "Highlight New Text",
                    description = "Briefly highlight each confirmed phrase",
                    checked = prefs.highlightLastChunk,
                    onCheckedChange = { viewModel.setHighlightLastChunk(it) }
                )
            }

            // System
            SettingsSection(title = "System") {
                SettingsToggle(
                    label = "Keep Screen On",
                    description = "Prevents sleep during active listening",
                    checked = prefs.keepScreenOn,
                    onCheckedChange = { viewModel.setKeepScreenOn(it) }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable Column.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                displayValue,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
