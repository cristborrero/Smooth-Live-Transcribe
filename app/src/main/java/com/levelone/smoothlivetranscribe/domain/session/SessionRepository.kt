package com.levelone.smoothlivetranscribe.domain.session

import kotlinx.coroutines.flow.Flow

data class Session(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val durationMs: Long
)

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun updateSession(session: Session)
    suspend fun deleteSession(id: String)
}
