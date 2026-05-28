package com.levelone.smoothlivetranscribe.domain.reading

/**
 * Represents the complete state of the reading presentation layer.
 *
 * Separation of confirmedText and partialText is the key anti-jump strategy:
 * - confirmedText: stable, won't change — rendered prominently
 * - partialText: ephemeral, may be revised — rendered with secondary style
 *
 * This prevents the "jump" problem: when partialText gets replaced by a new
 * partial result, only the secondary-styled area changes. The confirmed area
 * is a stable anchor the reader's eye can rest on.
 *
 * @param confirmedText All finalized transcription segments concatenated.
 * @param partialText Current interim result from the recognizer (will change).
 * @param autoFollowEnabled Whether the view is auto-scrolling to follow new text.
 * @param isHighlightingLastChunk Whether the last confirmed chunk should be visually highlighted.
 */
data class ReadingState(
    val confirmedText: String = "",
    val partialText: String = "",
    val autoFollowEnabled: Boolean = true,
    val isHighlightingLastChunk: Boolean = false,
    val lastConfirmedChunk: String = ""
) {
    /**
     * The full text to display, built by appending partial after confirmed.
     * Computing this here (not in the composable) avoids recomposition on every partial update
     * when the confirmed text hasn't changed.
     */
    val mergedVisibleText: String
        get() = if (partialText.isNotEmpty()) {
            if (confirmedText.isNotEmpty()) "$confirmedText $partialText"
            else partialText
        } else {
            confirmedText
        }

    val isEmpty: Boolean get() = confirmedText.isEmpty() && partialText.isEmpty()
}
