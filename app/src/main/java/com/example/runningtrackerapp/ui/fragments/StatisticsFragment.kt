package com.example.runningtrackerapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.ui.viewmodels.StatisticsViewModel
import com.example.runningtrackerapp.utility.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlinx.android.synthetic.main.item_run.*
import kotlin.math.round


@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserver()
    }

    private fun subscribeToObserver(){

        statisticsViewModel.totalRunTime.observe(viewLifecycleOwner, Observer {
            it?.let {
                val timeInMillis = it
                val totalTime = TrackingUtility().getFormattedStopWatchTimer(timeInMillis)
                tvTotalTime.text = totalTime
            }
        })

        statisticsViewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val distance = "${totalDistance}km"
                tvTotalDistance.text = distance
            }
        })

        statisticsViewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalAvgSpeed = round(it * 10f) /10f
                val speed = "${totalAvgSpeed}kmh"
                tvAverageSpeed.text = speed
            }
        })

        statisticsViewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val caloriesBurned = "${it}kcal"
                tvTotalCalories.text = caloriesBurned
            }
        })

    }
}