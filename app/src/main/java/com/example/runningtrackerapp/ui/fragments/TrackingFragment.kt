package com.example.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.ACTION_PAUSE
import com.example.runningtrackerapp.constant.Constants.ACTION_START_OR_RESUME
import com.example.runningtrackerapp.constant.Constants.ACTION_STOP
import com.example.runningtrackerapp.constant.Constants.MAP_ZOOM
import com.example.runningtrackerapp.constant.Constants.POLYLINE_COLOR
import com.example.runningtrackerapp.constant.Constants.POLYLINE_WITH
import com.example.runningtrackerapp.db.model.Run
import com.example.runningtrackerapp.services.Polyline
import com.example.runningtrackerapp.services.TrackingService
import com.example.runningtrackerapp.ui.viewmodels.MainViewModel
import com.example.runningtrackerapp.utility.CancelTrackingDialog
import com.example.runningtrackerapp.utility.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val FRAGMENT_MENEGER_TAG = "fragment_meneger"

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false

    @set:Inject
    var weight = 80f

    private var pathPoints = mutableListOf<Polyline>()

    private var currentTimeInMillis = 0L

    private var map: GoogleMap?= null

    private var menu: Menu?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager
                .findFragmentByTag(FRAGMENT_MENEGER_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
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
            val formattedTime = TrackingUtility().getFormattedStopWatchTimer(currentTimeInMillis,true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if (isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME)
        }
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0){
            btnToggleRun.text = "Continue"
            btnFinishRun.visibility = View.VISIBLE
        }else if(!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.GONE
        }
        else{
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints){
            for(pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb(){

        map?.snapshot { bmp ->
            var distanceInMeters = 0

            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility().calculatePolylineLength(polyline).toInt()
            }
            val evgSpeed = round((distanceInMeters / 1000f) /((currentTimeInMillis / 1000f) / 3600) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toLong()
            val run = Run(bmp,dateTimeStamp,evgSpeed,distanceInMeters,currentTimeInMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Add to Database Successfully",
                Snackbar.LENGTH_SHORT
            ).show()
            stopRun()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_cancel_run ->
                showCancelTrackingDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelTrackingDialog(){

        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager,FRAGMENT_MENEGER_TAG)

    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP)
        navHostFragment.findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
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