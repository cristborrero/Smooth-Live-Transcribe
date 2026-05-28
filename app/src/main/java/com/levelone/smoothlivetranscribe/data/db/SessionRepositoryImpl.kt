package com.levelone.smoothlivetranscribe.data.db

import com.levelone.smoothlivetranscribe.domain.session.Session
import com.levelone.smoothlivetranscribe.domain.session.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {

    override fun getAllSessions(): Flow<List<Session>> =
        dao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override suspend fun getSessionById(id: String): Session? =
        dao.getSessionById(id)?.toDomain()

    override suspend fun saveSession(session: Session) =
        dao.insertSession(session.toEntity())

    override suspend fun updateSession(session: Session) =
        dao.updateSession(session.toEntity())

    override suspend fun deleteSession(id: String) =
        dao.deleteSessionById(id)

    private fun SessionEntity.toDomain() = Session(
        id = id, title = title, content = content,
        createdAt = createdAt, durationMs = durationMs
    )

    private fun Session.toEntity() = SessionEntity(
        id = id, title = title, content = content,
        createdAt = createdAt, durationMs = durationMs
    )
}
