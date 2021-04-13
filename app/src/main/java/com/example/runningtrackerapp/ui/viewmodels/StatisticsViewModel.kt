package com.example.runningtrackerapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.runningtrackerapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val totalRunTime = mainRepository.getTotalTime()
    val totalDistance = mainRepository.getTotalDistance()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()
    val totalCaloriesBurned = mainRepository.getTotalCalories()
}