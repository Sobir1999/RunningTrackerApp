package com.example.runningtrackerapp.repositories

import com.example.runningtrackerapp.db.RunDAO
import com.example.runningtrackerapp.db.model.Run
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao: RunDAO
) {

    suspend fun insert(run: Run) = runDao.insert(run)

    suspend fun delete(run: Run) = runDao.delete(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByCalories() = runDao.getAllRunsSortedByCalories()

    fun getAllRunsSortedBySpeed() = runDao.getAllRunsSortedBySpeed()

    fun getAllRunsSortedByTime() = runDao.getAllRunsSortedByTime()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalTime() = runDao.getTotalTime()

    fun getTotalCalories() = runDao.getTotalCalories()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

}