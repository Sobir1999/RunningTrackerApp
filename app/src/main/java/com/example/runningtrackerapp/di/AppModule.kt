package com.example.runningtrackerapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningtrackerapp.constant.Constants.DATABASE_NAME
import com.example.runningtrackerapp.constant.Constants.FIRST_TOGGLE_RUN
import com.example.runningtrackerapp.constant.Constants.PERSON_NAME
import com.example.runningtrackerapp.constant.Constants.PERSON_WEIGHT
import com.example.runningtrackerapp.constant.Constants.SHARED_PREFERENCES_NAME
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

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) = app.getSharedPreferences(
        SHARED_PREFERENCES_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(PERSON_NAME,"") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(PERSON_WEIGHT,80f)

    @Singleton
    @Provides
    fun provideFirstToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(FIRST_TOGGLE_RUN,true)
}