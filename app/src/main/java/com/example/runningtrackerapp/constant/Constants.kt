package com.example.runningtrackerapp.constant

import android.graphics.Color

object Constants {

    const val DATABASE_NAME = "run_db"

    const val REQUEST_CODE = 100

    const val ACTION_START_OR_RESUME = "ACTION_START_OR_RESUME"
    const val ACTION_PAUSE = "ACTION_PAUSE"
    const val ACTION_STOP = "ACTION_STOP"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
    const val ACTION_TO_TRACKING_FRAGMENT = "ACTION_TO_TRACKING_FRAGMENT"

    const val UPDATE_LOCATION_INTERVAL = 5000L
    const val FASTEST_LOCATON_INTERVAL =  2000L

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WITH = 8f
    const val MAP_ZOOM = 15f

    const val UPDATE_TIMER_INTERVAL = 50L
}