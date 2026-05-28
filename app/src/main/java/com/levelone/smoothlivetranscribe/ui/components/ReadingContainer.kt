package com.levelone.smoothlivetranscribe.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.levelone.smoothlivetranscribe.core.ui.theme.PartialTextDark
import com.levelone.smoothlivetranscribe.core.ui.theme.Primary
import com.levelone.smoothlivetranscribe.domain.reading.ReadingState
import kotlinx.coroutines.launch

/**
 * ReadingContainer — The main display composable for transcribed text.
 *
 * ANTI-JUMP STRATEGY (critical for product value):
 *
 * 1. STABLE CONFIRMED ZONE: The confirmed text is rendered as a single AnnotatedString
 *    block. It only ever grows — never changes in place. The layout engine can cache
 *    the measured text and only lay out the new suffix.
 *
 * 2. PARTIAL ZONE: Partial text is rendered inline after confirmed text with a distinct
 *    visual style (lighter color, italic). When partial text changes, only the suffix of
 *    the AnnotatedString changes — the confirmed prefix stays constant in memory.
 *
 * 3. SCROLL ANIMATION via ScrollState + animateScrollTo (suspend function):
 *    - We track [scrollVersion] from ReadingEngine.
 *    - On each new version, we animate to maxValue using a spring animation.
 *    - Spring spec: low stiffness, low damping → smooth deceleration (like a physical scroll).
 *    - This is NEVER a scrollTo() — always animated.
 *
 * 4. MANUAL SCROLL DETECTION via detectVerticalDragGestures:
 *    - When user drags, we call [onUserScrolled] → ReadingEngine.pauseAutoFollow().
 *    - Auto-follow is only re-enabled by explicit "Back to Live" tap.
 *
 * 5. RECOMPOSITION OPTIMIZATION:
 *    - AnnotatedString is rebuilt only when [readingState] changes.
 *    - The ScrollState is read in a LaunchedEffect, not in the composition path.
 *    - No unnecessary state reads in the main composition scope.
 *
 * @param readingState Current text state from ReadingEngine.
 * @param scrollVersion Incremented by ReadingEngine when a scroll animation should trigger.
 * @param fontSizeSp User-configured font size.
 * @param lineHeightMultiplier User-configured line height multiplier.
 * @param showPartial Whether to render partial text.
 * @param onUserScrolled Called when user initiates manual scroll — signals ReadingEngine to pause auto-follow.
 * @param modifier Layout modifier.
 * @param contentPadding Internal padding for the reading area.
 */
@Composable
fun ReadingContainer(
    readingState: ReadingState,
    scrollVersion: Int,
    fontSizeSp: Float = 22f,
    lineHeightMultiplier: Float = 1.6f,
    showPartial: Boolean = true,
    highlightLastChunk: Boolean = true,
    onUserScrolled: () -> Unit = {},
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 24.dp)
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Track the last scrollVersion we acted on — avoids re-triggering for same version
    var lastHandledScrollVersion by remember { mutableIntStateOf(-1) }

    // Build AnnotatedString once per state change (not per scroll event)
    val annotatedText = remember(readingState, fontSizeSp, lineHeightMultiplier, showPartial, highlightLastChunk) {
        buildAnnotatedString {
            // Confirmed text — primary style, full weight
            if (readingState.confirmedText.isNotEmpty()) {
                if (highlightLastChunk && readingState.isHighlightingLastChunk && readingState.lastConfirmedChunk.isNotEmpty()) {
                    // Render everything BEFORE the last chunk normally
                    val beforeLastChunk = readingState.confirmedText.removeSuffix(readingState.lastConfirmedChunk).trimEnd()
                    if (beforeLastChunk.isNotEmpty()) {
                        withStyle(SpanStyle(
                            fontSize = fontSizeSp.sp,
                            fontWeight = FontWeight.Normal
                        )) {
                            append(beforeLastChunk)
                            append(" ")
                        }
                    }
                    // Render last chunk with highlight
                    withStyle(SpanStyle(
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.SemiBold,
                        background = Primary.copy(alpha = 0.25f)
                    )) {
                        append(readingState.lastConfirmedChunk)
                    }
                } else {
                    withStyle(SpanStyle(
                        fontSize = fontSizeSp.sp,
                        fontWeight = FontWeight.Normal
                    )) {
                        append(readingState.confirmedText)
                    }
                }
            }

            // Partial text — secondary style, visually distinct
            if (showPartial && readingState.partialText.isNotEmpty()) {
                if (readingState.confirmedText.isNotEmpty()) append(" ")
                withStyle(SpanStyle(
                    fontSize = (fontSizeSp * 0.95f).sp,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    color = PartialTextDark
                )) {
                    append(readingState.partialText)
                }
            }
        }
    }

    // Auto-scroll: triggered by scrollVersion changes from ReadingEngine
    LaunchedEffect(scrollVersion) {
        if (scrollVersion != lastHandledScrollVersion && readingState.autoFollowEnabled) {
            lastHandledScrollVersion = scrollVersion
            // Spring animation: feels natural, decelerates smoothly — not a hard jump
            // stiffness=Low means gradual acceleration + deceleration → teleprompter feel
            coroutineScope.launch {
                scrollState.animateScrollTo(
                    value = scrollState.maxValue,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Detect manual scroll gestures to pause auto-follow
                detectVerticalDragGestures { _, dragAmount ->
                    // Only pause if user is scrolling UP (reading back) with significant drag
                    if (dragAmount < -10f && readingState.autoFollowEnabled) {
                        onUserScrolled()
                    }
                }
            }
    ) {
        Text(
            text = annotatedText,
            style = TextStyle(
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp * lineHeightMultiplier).sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(contentPadding)
        )
    }
}
