package com.levelone.smoothlivetranscribe.di

import android.content.Context
import androidx.room.Room
import com.levelone.smoothlivetranscribe.data.db.AppDatabase
import com.levelone.smoothlivetranscribe.data.db.SessionDao
import com.levelone.smoothlivetranscribe.data.db.SessionRepositoryImpl
import com.levelone.smoothlivetranscribe.data.speech.SpeechTranscriptionRepository
import com.levelone.smoothlivetranscribe.domain.session.SessionRepository
import com.levelone.smoothlivetranscribe.domain.transcription.TranscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smooth_transcribe_db"
        ).build()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionRepository(
        impl: SpeechTranscriptionRepository
    ): TranscriptionRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository
}
