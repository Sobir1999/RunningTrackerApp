package com.example.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.ACTION_PAUSE
import com.example.runningtrackerapp.constant.Constants.ACTION_START_OR_RESUME
import com.example.runningtrackerapp.constant.Constants.MAP_ZOOM
import com.example.runningtrackerapp.constant.Constants.POLYLINE_COLOR
import com.example.runningtrackerapp.constant.Constants.POLYLINE_WITH
import com.example.runningtrackerapp.services.Polyline
import com.example.runningtrackerapp.services.TrackingService
import com.example.runningtrackerapp.ui.viewmodels.MainViewModel
import com.example.runningtrackerapp.utility.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*


@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var currentTimeInMillis = 0L

    private var map: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoint.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLastPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
        })
    }

    private fun toggleRun(){
        if (isTracking){
            sendCommandToService(ACTION_PAUSE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME)
        }
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if (!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }else{
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WITH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLastPolyline(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WITH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}