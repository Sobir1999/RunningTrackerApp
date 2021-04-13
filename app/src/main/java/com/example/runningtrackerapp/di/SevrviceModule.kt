package com.example.runningtrackerapp.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants
import com.example.runningtrackerapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object SevrviceModule {

    @Provides
    @ServiceScoped
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @Provides
    @ServiceScoped
    fun provideGetMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity(app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_TO_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    @Provides
    @ServiceScoped
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Running Tracker App")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)

}