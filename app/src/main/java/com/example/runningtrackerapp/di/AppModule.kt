package com.example.runningtrackerapp.di

import android.content.Context
import androidx.room.Room
import com.example.runningtrackerapp.constant.Constants.DATABASE_NAME
import com.example.runningtrackerapp.db.RunDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(app,RunDatabase::class.java,DATABASE_NAME)
        .build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunDatabase) = db.getRunDao()
}