package com.example.runningtrackerapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningtrackerapp.R
import com.example.runningtrackerapp.constant.Constants.ACTION_PAUSE
import com.example.runningtrackerapp.constant.Constants.ACTION_START_OR_RESUME
import com.example.runningtrackerapp.constant.Constants.ACTION_STOP
import com.example.runningtrackerapp.constant.Constants.ACTION_TO_TRACKING_FRAGMENT
import com.example.runningtrackerapp.constant.Constants.FASTEST_LOCATON_INTERVAL
import com.example.runningtrackerapp.constant.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningtrackerapp.constant.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningtrackerapp.constant.Constants.NOTIFICATION_ID
import com.example.runningtrackerapp.constant.Constants.UPDATE_LOCATION_INTERVAL
import com.example.runningtrackerapp.constant.Constants.UPDATE_TIMER_INTERVAL
import com.example.runningtrackerapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService: LifecycleService() {

    private var isFirstRun = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoint = MutableLiveData<Polylines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoint.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME ->{
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    }else {
                        startTimer()
                    }
                }
                ACTION_PAUSE ->{
                    pauseService()
                }
                ACTION_STOP ->{
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTimerEnabled = true
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(UPDATE_TIMER_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude,location.longitude)
            pathPoint.value?.apply {
                last().add(pos)
                pathPoint.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoint.value?.apply {
        add(mutableListOf())
        pathPoint.postValue(this)
    } ?: pathPoint.postValue(mutableListOf(mutableListOf()))

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){
        if (isTracking){
            val request = LocationRequest().apply {
                interval = UPDATE_LOCATION_INTERVAL
                fastestInterval = FASTEST_LOCATON_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }

            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!){
                result.locations?.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    private fun startForegroundService(){

        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running Tracker App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(this,
        0,
        Intent(this,MainActivity::class.java).also {
            it.action = ACTION_TO_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
        )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}