package com.levelone.smoothlivetranscribe.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved transcription session.
 *
 * Design decision: content is stored as plain String (not normalized into words/segments)
 * because the primary use case is review and export — not searchability or analysis.
 * Keeping it simple avoids over-engineering at this scale.
 *
 * @param id UUID string for globally unique identification
 * @param title User-editable session title (defaults to timestamp-based name)
 * @param content Full transcription text (confirmed segments only — not partials)
 * @param createdAt Unix timestamp in millis
 * @param durationMs Length of the recording session in milliseconds
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val durationMs: Long
)
