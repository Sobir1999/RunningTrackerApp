package com.example.runningtrackerapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.runningtrackerapp.db.model.Run


@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: Run)

    @Delete
    suspend fun delete(run: Run)

    @Query("SELECT * FROM run_table ORDER by timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER by distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER by avgSpeedInKMH DESC")
    fun getAllRunsSortedBySpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER by caloriesBurned DESC")
    fun getAllRunsSortedByCalories(): LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER by timeInMillis DESC")
    fun getAllRunsSortedByTime(): LiveData<List<Run>>

    @Query("SELECT SUM(distanceInMeters) FROM run_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT SUM(caloriesBurned) FROM run_table")
    fun getTotalCalories(): LiveData<Int>

    @Query("SELECT SUM(timeInMillis) FROM run_table")
    fun getTotalTime(): LiveData<Long>

    @Query("SELECT SUM(avgSpeedInKMH) FROM run_table")
    fun getTotalAvgSpeed(): LiveData<Float>
}