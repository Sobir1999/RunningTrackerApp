package com.example.runningtrackerapp.utility

import android.content.Context
import android.os.Build

object TrackingUtility {

    fun hasLocationPermission(context: Context){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
        }
    }
}