package com.example.runningtrackerapp.db.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class Run(
    var img: Bitmap? = null,
    var timestamp: Long = 0,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0,
    var caloriesBurned: Long = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}