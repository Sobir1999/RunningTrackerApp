package com.example.runningtrackerapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtrackerapp.constant.SortedType
import com.example.runningtrackerapp.db.model.Run
import com.example.runningtrackerapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedBySpeed()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByTime = mainRepository.getAllRunsSortedByTime()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCalories()

    val run = MediatorLiveData<List<Run>>()
    var sortedType = SortedType.DATE

    init {
        run.addSource(runsSortedByDate){ result->
            if (sortedType == SortedType.DATE){
                result?.let { run.value = it }
            }
        }

        run.addSource(runsSortedByTime){ result->
            if (sortedType == SortedType.TIME){
                result?.let { run.value = it }
            }
        }

        run.addSource(runsSortedByAvgSpeed){ result->
            if (sortedType == SortedType.AVG_SPEED){
                result?.let { run.value = it }
            }
        }

        run.addSource(runsSortedByDistance){ result->
            if (sortedType == SortedType.DISTANCE){
                result?.let { run.value = it }
            }
        }

        run.addSource(runsSortedByCaloriesBurned){ result->
            if (sortedType == SortedType.CLORIES_BURNED){
                result?.let { run.value = it }
            }
        }
    }

    fun sortRun(sortedType: SortedType){
        when(sortedType){
            SortedType.DATE -> runsSortedByDate.value?.let { run.value = it }
            SortedType.TIME -> runsSortedByTime.value?.let { run.value = it }
            SortedType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { run.value = it }
            SortedType.DISTANCE -> runsSortedByDistance.value?.let { run.value = it }
            SortedType.CLORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { run.value = it }
        }.also {
            this.sortedType = sortedType
        }
    }

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insert(run)
    }
}